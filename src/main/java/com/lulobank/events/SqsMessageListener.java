package com.lulobank.events;

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


public class SqsMessageListener implements MessageListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(SqsMessageListener.class);
    private final SqsClient sqsClient;
    private static final String QUEUE_URL = "https://sqs.us-east-1.amazonaws.com/900852371335/cduarte-queue";
    private final Scheduler taskScheduler = Schedulers.newBoundedElastic(10, 100, "taskThread");

    private final Scheduler subscribeScheduler = Schedulers.newBoundedElastic(10, 100, "subscribeThread");

    public SqsMessageListener(SqsClient sqsClient) {
        this.sqsClient = sqsClient;
    }

    public void listen(int concurrency, Function<String, Either<?, Void>> task) {
        receiveMessages()
                .flatMapIterable(Function.identity())
                .doOnError(this::handlerErrorReceivingMessages)
                .retry()
                .flatMap(
                        //TODO refactor this using extract method
                        message -> Mono.fromSupplier(() -> task.apply(message.body()))
                        .doOnNext(either -> either.fold(
                                left -> Mono.just(left)
                                        .then(changeVisibilityTimeout(message.receiptHandle()))
                                        //TODO: Error handling, take into account the backoff strategy provided by the SDK
                                        .doOnNext(e -> LOGGER.debug("Changed visibility timeout: {}", message.body()))
                                        .subscribe(),

                                right -> Mono.justOrEmpty(right)
                                        .then(deleteQueueMessage(message.receiptHandle()))
                                        //TODO: Error handling, take into account the backoff strategy provided by the SDK
                                        .doOnNext(e -> LOGGER.debug("Deleted message: {}", message.body()))
                                        .subscribe()
                        ))
                        .onErrorResume(t -> {
                            //TODO make it more fluent
                            LOGGER.error(t.getMessage());
                            return Mono.empty();
                        })
                        .then()
                        .subscribeOn(taskScheduler), concurrency)
                .subscribeOn(subscribeScheduler)
                .subscribe();
    }

    private Flux<List<Message>> receiveMessages() {
        return Flux.generate(
                (SynchronousSink<List<Message>> sink) -> {
                    ReceiveMessageRequest receiveMessageRequest = ReceiveMessageRequest.builder()
                            .queueUrl(QUEUE_URL)
                            .maxNumberOfMessages(5)
                            .waitTimeSeconds(20)
                            .build();
                    List<Message> messages = sqsClient.receiveMessage(receiveMessageRequest).messages();
                    LOGGER.debug("Received: {}", messages);
                    sink.next(messages);
                }
        );
    }

    private void handlerErrorReceivingMessages(Throwable t) {
        LOGGER.error(t.getMessage());
    }

    private Mono<SqsResponse> deleteQueueMessage(String receiptHandle) {
        return Mono.just(sqsClient.deleteMessage(builder -> builder
                .queueUrl(QUEUE_URL)
                .receiptHandle(receiptHandle)));
    }

    private Mono<SqsResponse> changeVisibilityTimeout(String receiptHandle) {
        return Mono.just(sqsClient.changeMessageVisibility(builder -> builder
                .queueUrl(QUEUE_URL)
                .receiptHandle(receiptHandle)
                .visibilityTimeout(5)));
    }
}
