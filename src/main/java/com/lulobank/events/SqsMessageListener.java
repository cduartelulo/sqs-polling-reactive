package com.lulobank.events;

import com.lulobank.events.config.SQSListenerProperties;
import io.vavr.control.Either;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.SynchronousSink;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;
import software.amazon.awssdk.services.sqs.model.SqsResponse;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import static reactor.core.scheduler.Schedulers.DEFAULT_BOUNDED_ELASTIC_QUEUESIZE;
import static reactor.core.scheduler.Schedulers.DEFAULT_BOUNDED_ELASTIC_SIZE;


public class SqsMessageListener implements MessageListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(SqsMessageListener.class);
    private static final int DEFAULT_MAX_NUMBER_OF_MESSAGES = 5;
    private static final int DEFAULT_WAIT_TIME_SECONDS = 20;
    private final SqsClient sqsClient;
    private final SQSListenerProperties.SQS.Listener listenerProperties;

    // TODO: Review if subscribeScheduler is needed, if so, review the parameters
    private final Scheduler subscribeScheduler = Schedulers.newBoundedElastic(DEFAULT_BOUNDED_ELASTIC_SIZE, DEFAULT_BOUNDED_ELASTIC_QUEUESIZE, "subscribeThread");
    private final int DEFAULT_CONCURRENCY = Runtime.getRuntime().availableProcessors();
    private Scheduler taskScheduler;

    public SqsMessageListener(SqsClient sqsClient, SQSListenerProperties.SQS.Listener listenerProperties) {
        this.sqsClient = sqsClient;
        this.listenerProperties = listenerProperties;
    }

    @Override
    public void listen(Function<Message, Either<?, Void>> eventHandler) {
        receiveMessages()
                .flatMapIterable(Function.identity())
                .doOnError(this::handleReceivingMessagesError)
                .retry()
                .flatMap(message -> processMessage(message, eventHandler), getConcurrency())
                .subscribeOn(subscribeScheduler)
                .subscribe();
    }

    @Override
    public Flux<List<Message>> receiveMessages() {
        return Flux.generate(
                (SynchronousSink<List<Message>> sink) -> {
                    ReceiveMessageRequest receiveMessageRequest = ReceiveMessageRequest.builder()
                            .queueUrl(listenerProperties.getQueueURL())
                            .maxNumberOfMessages(getMaxNumberOfMessages())
                            .waitTimeSeconds(getWaitTimeSeconds())
                            .build();
                    List<Message> messages = sqsClient.receiveMessage(receiveMessageRequest).messages();
                    LOGGER.debug("Received: {}", messages);
                    sink.next(messages);
                }
        );
    }

    @Override
    public Mono<Void> processMessage(Message message, Function<Message, Either<?, Void>> eventHandler) {
        return Mono.fromSupplier(() -> eventHandler.apply(message))
                .doOnNext(either -> either.fold(
                        left -> handleEventHandlerError(message, either)
                                .subscribe(),
                        right -> handleEventHandlerSuccess(message, either)
                                .subscribe()
                ))
                .onErrorResume(t -> {
                    //TODO make it more fluent, review the fallback strategy
                    LOGGER.error(t.getMessage());
                    return Mono.empty();
                })
                .then()
                .subscribeOn(getTaskScheduler());
    }

    //TODO: Research about threadCap and queueSize, review if default values are ok
    @Override
    public Scheduler getTaskScheduler() {
        if (Objects.isNull(taskScheduler)) {
            taskScheduler = Schedulers.newBoundedElastic(getMaximumNumberOfThreads(), getMaximumQueueCapacity(), "taskThread");
        }
        return taskScheduler;
    }

    @Override
    public int getMaximumNumberOfThreads() {
        return listenerProperties.getMaximumNumberOfThreads() != 0 ? listenerProperties.getMaximumNumberOfThreads() : DEFAULT_BOUNDED_ELASTIC_SIZE;
    }

    @Override
    public int getConcurrency() {
        return listenerProperties.getConcurrency() != 0 ? listenerProperties.getConcurrency() : DEFAULT_CONCURRENCY;
    }

    @Override
    public int getMaximumQueueCapacity() {
        return listenerProperties.getMaximumQueueCapacity() != 0 ? listenerProperties.getMaximumQueueCapacity() : DEFAULT_BOUNDED_ELASTIC_QUEUESIZE;
    }

    @Override
    public int getMaxNumberOfMessages() {
        return listenerProperties.getMaxNumberOfMessages() != 0 ? listenerProperties.getMaxNumberOfMessages() : DEFAULT_MAX_NUMBER_OF_MESSAGES;
    }

    @Override
    public int getWaitTimeSeconds() {
        return listenerProperties.getWaitTimeSeconds() != 0 ? listenerProperties.getWaitTimeSeconds() : DEFAULT_WAIT_TIME_SECONDS;
    }

    private Mono<SqsResponse> handleEventHandlerSuccess(Message message, Either<?, Void> eventHandlerResult) {
        return Mono.justOrEmpty(eventHandlerResult.get())
                .then(deleteQueueMessage(message.receiptHandle()))
                //TODO: Error handling, take into account the backoff strategy provided by the SDK, maybe a fallback strategy?
                .doOnNext(e -> LOGGER.debug("Deleted message: {}", message.body()));
    }

    private Mono<SqsResponse> handleEventHandlerError(Message message, Either<?, Void> eventHandlerResult) {
        return Mono.just(eventHandlerResult.getLeft())
                .then(changeVisibilityTimeout(message.receiptHandle()))
                //TODO: Error handling, take into account the backoff strategy provided by the SDK, maybe a fallback strategy?
                .doOnNext(e -> LOGGER.debug("Changed visibility timeout: {}", message.body()));
    }

    private void handleReceivingMessagesError(Throwable t) {
        LOGGER.error(t.getMessage());
    }

    private Mono<SqsResponse> deleteQueueMessage(String receiptHandle) {
        return Mono.just(sqsClient.deleteMessage(builder -> builder
                .queueUrl(listenerProperties.getQueueURL())
                .receiptHandle(receiptHandle)));
    }

    private Mono<SqsResponse> changeVisibilityTimeout(String receiptHandle) {
        return Mono.just(sqsClient.changeMessageVisibility(builder -> builder
                .queueUrl(listenerProperties.getQueueURL())
                .receiptHandle(receiptHandle)
                .visibilityTimeout(listenerProperties.getVisibilityTimeout())));
    }
}
