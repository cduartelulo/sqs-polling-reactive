package com.lulobank.events.impl.receiver;

import com.lulobank.events.api.receiver.Message;
import com.lulobank.events.api.receiver.MessageReceiver;
import io.vavr.control.Try;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.SynchronousSink;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import static reactor.core.scheduler.Schedulers.DEFAULT_BOUNDED_ELASTIC_QUEUESIZE;
import static reactor.core.scheduler.Schedulers.DEFAULT_BOUNDED_ELASTIC_SIZE;

/**
 * SQS message receiver in a reactive way, receives messages from SQS queue and process them
 * @author Carlos Duarte
 */
public class SqsMessageReceiver implements MessageReceiver {

    private static final Logger LOGGER = LoggerFactory.getLogger(SqsMessageReceiver.class);

    private static final int DEFAULT_MAX_NUMBER_OF_MESSAGES = 10;

    private static final int DEFAULT_WAIT_TIME_SECONDS = 20;

    private static final int DEFAULT_CONCURRENCY = Runtime.getRuntime().availableProcessors();

    private final SqsClient sqsClient;

    private final String queueUrl;

    private final SqsReceiverProperties sqsReceiverProperties;

    private final Scheduler subscribeScheduler = Schedulers.newBoundedElastic(DEFAULT_BOUNDED_ELASTIC_SIZE, DEFAULT_BOUNDED_ELASTIC_QUEUESIZE, "subscribeThread");

    private Scheduler taskScheduler;

    public SqsMessageReceiver(SqsClient sqsClient, String queueUrl, SqsReceiverProperties sqsReceiverProperties) {
        this.sqsClient = sqsClient;
        this.queueUrl = queueUrl;
        this.sqsReceiverProperties = sqsReceiverProperties;
    }

    @Override
    public void executePoll(BiFunction<String, Map<String, String>, Try<Void>> listener) {
        receiveMessages()
                .flatMapIterable(Function.identity())
                .doOnError(this::handleReceivingMessagesError)
                .retry()
                .flatMap(message -> processMessage(message, listener), getMaxConcurrentMessages())
                .subscribeOn(subscribeScheduler)
                .subscribe();
    }

    @Override
    public Flux<List<Message>> receiveMessages() {
        return Flux.generate(
                (SynchronousSink<List<Message>> sink) -> {
                    ReceiveMessageRequest receiveMessageRequest = ReceiveMessageRequest.builder()
                            .queueUrl(queueUrl)
                            .maxNumberOfMessages(getMaxNumberOfMessages())
                            .waitTimeSeconds(getWaitTimeSeconds())
                            .build();

                    List<Message> messages = sqsClient.receiveMessage(receiveMessageRequest)
                            .messages()
                            .stream()
                            .map(message -> new Message(message.messageId(), message.body(), message.attributesAsStrings(), message.receiptHandle()))
                            .collect(Collectors.toList());

                    LOGGER.debug("Received: {}", messages);
                    sink.next(messages);
                }
        );
    }

    @Override
    public Mono<Void> processMessage(Message message, BiFunction<String, Map<String, String>, Try<Void>> listener) {
        return Mono.fromSupplier(() -> listener.apply(message.getBody(), message.getAttributes()))
                .doOnNext(r -> r.onSuccess(e -> deleteQueueMessage(message.getReceiptHandle()))
                        .onFailure(e -> changeVisibilityTimeout(message.getReceiptHandle())))
                .onErrorResume(t -> {
                    LOGGER.error(t.getMessage());
                    return Mono.empty();
                })
                .then()
                .subscribeOn(getTaskScheduler());
    }

    @Override
    public Scheduler getTaskScheduler() {
        if (Objects.isNull(taskScheduler)) {
            taskScheduler = Schedulers.newBoundedElastic(getThreadCap(),
                    getQueuedTaskCap(), "taskThread");
        }
        return taskScheduler;
    }

    @Override
    public Scheduler getSubscribeScheduler() {
        return subscribeScheduler;
    }

    private void handleReceivingMessagesError(Throwable t) {
        LOGGER.error(t.getMessage());
    }

    private void deleteQueueMessage(String receiptHandle) {
        Try.of(() -> sqsClient.deleteMessage(builder -> builder
                        .queueUrl(queueUrl)
                        .receiptHandle(receiptHandle)))
                .onSuccess(e -> LOGGER.debug("Deleted message: {}", receiptHandle))
                .onFailure(e -> LOGGER.error("Error deleting message: {}", e.getMessage()))
                .get();

    }

    private void changeVisibilityTimeout(String receiptHandle) {
        Try.of(() -> sqsClient.changeMessageVisibility(builder -> builder
                        .queueUrl(queueUrl)
                        .receiptHandle(receiptHandle)
                        .visibilityTimeout(getVisibilityTimeout())))
                .onSuccess(e -> LOGGER.debug("Changed visibility timeout: {}", receiptHandle))
                .onFailure(e -> LOGGER.error("Error changing visibility timeout: {}", e.getMessage()))
                .get();
    }

    private int getMaxNumberOfMessages() {
        int maxNumberOfMessages = sqsReceiverProperties.getMaxMessagesPerPoll().getOrElse(DEFAULT_MAX_NUMBER_OF_MESSAGES);
        Assert.isTrue(maxNumberOfMessages > 0, "maxMessagesPerPoll must be greater than 0");
        return Math.min(maxNumberOfMessages, DEFAULT_MAX_NUMBER_OF_MESSAGES);
    }

    private int getWaitTimeSeconds() {
        int waitTimeSeconds = sqsReceiverProperties.getMaxWaitTimeSecondsPerPoll().getOrElse(DEFAULT_WAIT_TIME_SECONDS);
        Assert.isTrue(waitTimeSeconds > 0, "maxWaitTimeoutSecondsPerPoll must be greater than 0");
        return Math.min(waitTimeSeconds, DEFAULT_WAIT_TIME_SECONDS);
    }

    private int getVisibilityTimeout() {
        int visibilityTimeout = sqsReceiverProperties.getRetryDelaySeconds().getOrElse(0);
        Assert.isTrue(visibilityTimeout > 0, "retryDelaySeconds must be greater than 0");
        return visibilityTimeout;
    }

    private int getMaxConcurrentMessages() {
        int maxConcurrentMessages = sqsReceiverProperties.getMaxConcurrentMessages().getOrElse(DEFAULT_CONCURRENCY);
        Assert.isTrue(maxConcurrentMessages > 0, "maxConcurrentMessages must be greater than 0");
        return maxConcurrentMessages;
    }

    private int getThreadCap() {
        int maxNumberOfThreads = sqsReceiverProperties.getMaxNumberOfThreads().getOrElse(DEFAULT_BOUNDED_ELASTIC_SIZE);
        Assert.isTrue(maxNumberOfThreads > 0, "maxNumberOfThreads must be greater than 0");
        return maxNumberOfThreads;
    }

    private int getQueuedTaskCap() {
        int maxQueueCapacity = sqsReceiverProperties.getMaxQueueCapacity().getOrElse(DEFAULT_BOUNDED_ELASTIC_QUEUESIZE);
        Assert.isTrue(maxQueueCapacity > 0, "maxQueueCapacity must be greater than 0");
        return maxQueueCapacity;
    }
}
