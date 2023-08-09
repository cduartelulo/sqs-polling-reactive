package com.lulobank.events;

import io.vavr.Tuple3;
import io.vavr.control.Either;
import io.vavr.control.Try;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.SynchronousSink;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;
import software.amazon.awssdk.services.sqs.model.SqsException;

import java.time.Duration;
import java.util.List;
import java.util.function.Function;


public class SqsEventHandler implements EventHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(SqsEventHandler.class);
    private final SqsClient sqsClient;
    private static final String QUEUE_URL = "https://sqs.us-east-1.amazonaws.com/900852371335/cduarte-queue";
    private final Scheduler taskScheduler = Schedulers.newBoundedElastic(10, 100, "taskThread");

    public SqsEventHandler(SqsClient sqsClient) {
        this.sqsClient = sqsClient;
    }

    public void handle(int concurrency, Function<String, Mono<Either<String, String>>> task) {
        Flux.generate(
                (SynchronousSink<List<Message>> sink) -> {
                            ReceiveMessageRequest receiveMessageRequest = ReceiveMessageRequest.builder()
                                    .queueUrl(QUEUE_URL)
                                    .maxNumberOfMessages(5)
                                    .waitTimeSeconds(10)
                                    .build();

                            List<Message> messages = sqsClient.receiveMessage(receiveMessageRequest).messages();
                            LOGGER.info("Received: {}", messages);
                            sink.next(messages);
                        }
                )
                .flatMapIterable(Function.identity())
                .doOnError(t -> LOGGER.error(t.getMessage(), t))
                .retry()
                .map(message ->
                        new Tuple3<Message, Runnable, Runnable>(
                                message,
                                () -> deleteQueueMessage(message.receiptHandle()),
                                () -> changeVisibilityTimeout(message.receiptHandle())))
                .flatMap(
                        (Tuple3<Message, Runnable, Runnable> tuple) -> {
                            Message message = tuple._1;
                            Runnable deleteHandle = tuple._2;
                            Runnable changeVisibilityTimeoutHandle = tuple._3;
                            return task
                                    .apply(message.body())
                                    .doOnNext(either -> either.fold(
                                            left -> Mono.just(left)
                                                        .doOnNext(value -> LOGGER.info(either.getLeft()))
                                                        .delayElement(Duration.ofSeconds(5))
                                                        .then(Mono.fromSupplier(() -> Try.run(changeVisibilityTimeoutHandle::run)))
                                                        .doOnError(s -> LOGGER.error("Error while changing visibility timeout", s))
                                                        .doOnNext(s -> LOGGER.info("Message visibility changed"))
                                                        .subscribe(),

                                            right -> Mono.just(right)
                                                        .then(Mono.fromSupplier(() -> Try.run(deleteHandle::run)))
                                                        .doOnNext(value -> LOGGER.info(either.get()))
                                                        .subscribe()
                                    ))
                                    .onErrorResume(t -> {
                                        LOGGER.error(t.getMessage(), t);
                                        return Mono.empty();
                                    })
                                    .then()
                                    .subscribeOn(taskScheduler);
                        },concurrency)
                .subscribeOn(Schedulers.newBoundedElastic(10, 100, "taskThread"))
                .subscribe();
    }

    private void deleteQueueMessage(String receiptHandle) {
        try {
            DeleteMessageRequest deleteMessageRequest = DeleteMessageRequest.builder()
                    .queueUrl(QUEUE_URL)
                    .receiptHandle(receiptHandle)
                    .build();
            sqsClient.deleteMessage(deleteMessageRequest);
            LOGGER.info("Deleted queue message handler={}", receiptHandle);
        }
        catch (SqsException e) {
            LOGGER.error("Error while deleting message from queue={}", receiptHandle, e);
        }

    }

    private void changeVisibilityTimeout(String receiptHandle) {
        sqsClient.changeMessageVisibility(builder -> builder
                .queueUrl(QUEUE_URL)
                .receiptHandle(receiptHandle)
                .visibilityTimeout(0));
    }
}
