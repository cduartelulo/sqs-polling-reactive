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

import java.util.List;
import java.util.function.Function;


public class SqsMessageListener implements MessageListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(SqsMessageListener.class);
    private final SqsClient sqsClient;
    private static final String QUEUE_URL = "https://sqs.us-east-1.amazonaws.com/900852371335/cduarte-queue";
    private final Scheduler taskScheduler = Schedulers.newBoundedElastic(10, 100, "taskThread");

    public SqsMessageListener(SqsClient sqsClient) {
        this.sqsClient = sqsClient;
    }

    public void listen(int concurrency, Function<String, Either<String, String>> task) {
        Flux.generate(
                (SynchronousSink<List<Message>> sink) -> {
                            ReceiveMessageRequest receiveMessageRequest = ReceiveMessageRequest.builder()
                                    .queueUrl(QUEUE_URL)
                                    .maxNumberOfMessages(5)
                                    .waitTimeSeconds(20)
                                    .build();
                            List<Message> messages = sqsClient.receiveMessage(receiveMessageRequest).messages();
                            LOGGER.info("Received: {}", messages);
                            sink.next(messages);
                        }
                )
                .flatMapIterable(Function.identity())
                .doOnError(t -> LOGGER.error(t.getMessage()))
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
                            return Mono
                                    .fromSupplier(() -> task.apply(message.body()))
                                    .doOnNext(either -> either.fold(
                                            left -> Mono.just(left)
                                                        .then(Mono.fromSupplier(() -> Try.run(changeVisibilityTimeoutHandle::run)))
                                                        //TODO: Review doOnError and doOnNext
                                                        .then()
                                                        .subscribe(),

                                            right -> Mono.just(right)
                                                        //TODO: Review doOnError and doOnNext
                                                        .then(Mono.fromSupplier(() -> Try.run(deleteHandle::run)))
                                                        .then()
                                                        .subscribe()
                                    ))
                                    .onErrorResume(t -> {
                                        LOGGER.error(t.getMessage());
                                        return Mono.empty();
                                    })
                                    .then()
                                    .subscribeOn(taskScheduler);
                        },concurrency)
                .subscribeOn(Schedulers.newBoundedElastic(10, 100, "subscribeThread"))
                .subscribe();
    }

    private void deleteQueueMessage(String receiptHandle) {
        try {
            DeleteMessageRequest deleteMessageRequest = DeleteMessageRequest.builder()
                    .queueUrl(QUEUE_URL)
                    .receiptHandle(receiptHandle)
                    .build();
            sqsClient.deleteMessage(deleteMessageRequest);
            LOGGER.debug("Deleted queue message receiptHandle={}", receiptHandle);
        }
        catch (SqsException e) {
            LOGGER.error("Error while deleting message from queue={}", receiptHandle);
        }

    }

    private void changeVisibilityTimeout(String receiptHandle) {
        sqsClient.changeMessageVisibility(builder -> builder
                .queueUrl(QUEUE_URL)
                .receiptHandle(receiptHandle)
                .visibilityTimeout(5));
        LOGGER.debug("Message visibility changed for receiptHandle={}", receiptHandle);
    }
}
