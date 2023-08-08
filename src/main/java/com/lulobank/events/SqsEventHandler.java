package com.lulobank.events;

import io.vavr.Tuple2;
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


public class SqsEventHandler implements EventHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(SqsEventHandler.class);
    private final SqsClient sqsClient;
    private static final String QUEUE_URL = "https://sqs.us-east-1.amazonaws.com/900852371335/cduarte-queue";
    private final Scheduler taskScheduler = Schedulers.newBoundedElastic(10, 100, "taskThread");

    public SqsEventHandler(SqsClient sqsClient) {
        this.sqsClient = sqsClient;
    }

    public void handle(int concurrency, Function<String, Mono<Void>> task) {
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
                .map(message -> {
                    String messageBody = message.body();
                    return new Tuple2<String, Runnable>(messageBody, () -> deleteQueueMessage(message.receiptHandle()));
                })
                .flatMap(
                        (Tuple2<String, Runnable> tuple) -> {
                            String message = tuple._1;
                            Runnable deleteHandle = tuple._2;
                            return task
                                    .apply(message)
                                    .then(Mono.fromSupplier(() -> Try.run(deleteHandle::run)
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
}
