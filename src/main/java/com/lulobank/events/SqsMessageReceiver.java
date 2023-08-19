package com.lulobank.events;

import com.lulobank.events.config.ReceiverProperties;
import io.vavr.control.Either;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.SynchronousSink;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.*;

import java.util.List;
import java.util.function.Function;

import static reactor.core.scheduler.Schedulers.DEFAULT_BOUNDED_ELASTIC_QUEUESIZE;
import static reactor.core.scheduler.Schedulers.DEFAULT_BOUNDED_ELASTIC_SIZE;


public class SqsMessageReceiver implements MessageReceiver {
    private static final Logger LOGGER = LoggerFactory.getLogger(SqsMessageReceiver.class);
    private final SqsClient sqsClient;
    private final ReceiverProperties receiverProperties;
    private final Scheduler taskScheduler = Schedulers.newBoundedElastic(DEFAULT_BOUNDED_ELASTIC_SIZE, DEFAULT_BOUNDED_ELASTIC_QUEUESIZE, "taskThread");

    private final Scheduler subscribeScheduler = Schedulers.newBoundedElastic(DEFAULT_BOUNDED_ELASTIC_SIZE, DEFAULT_BOUNDED_ELASTIC_QUEUESIZE, "subscribeThread");

    public SqsMessageReceiver(SqsClient sqsClient, ReceiverProperties receiverProperties) {
        this.sqsClient = sqsClient;
        this.receiverProperties = receiverProperties;
    }


    @Override
    public void execute(Function<String, Either<?, Void>> eventHandler) {
        receiveMessages()
                .flatMapIterable(Function.identity())
                .doOnError(this::handlerErrorReceivingMessages)
                .retry()
                .flatMap(message -> processMessage(message, eventHandler), receiverProperties.getConcurrency())
                .subscribeOn(subscribeScheduler)
                .subscribe();
    }

    @Override
    public Flux<List<Message>> receiveMessages() {
        return Flux.generate(
                (SynchronousSink<List<Message>> sink) -> {
                    ReceiveMessageRequest receiveMessageRequest = ReceiveMessageRequest.builder()
                            .queueUrl(receiverProperties.getQueueURL())
                            .maxNumberOfMessages(5)
                            .waitTimeSeconds(20)
                            .build();
                    List<Message> messages = sqsClient.receiveMessage(receiveMessageRequest).messages();
                    LOGGER.debug("Received: {}", messages);
                    sink.next(messages);
                }
        );
    }

    @Override
    public Mono<Void> processMessage(Message message, Function<String, Either<?, Void>> eventHandler) {
        return Mono.fromSupplier(() -> eventHandler.apply(message.body()))
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
                .subscribeOn(taskScheduler);
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

    private void handlerErrorReceivingMessages(Throwable t) {
        LOGGER.error(t.getMessage());
    }

    private Mono<SqsResponse> deleteQueueMessage(String receiptHandle) {
        return Mono.just(sqsClient.deleteMessage(builder -> builder
                .queueUrl(receiverProperties.getQueueURL())
                .receiptHandle(receiptHandle)));
    }

    private Mono<SqsResponse> changeVisibilityTimeout(String receiptHandle) {
        return Mono.just(sqsClient.changeMessageVisibility(builder -> builder
                .queueUrl(receiverProperties.getQueueURL())
                .receiptHandle(receiptHandle)
                .visibilityTimeout(5)));
    }
}
