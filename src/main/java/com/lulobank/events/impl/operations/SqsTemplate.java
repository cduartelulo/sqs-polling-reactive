package com.lulobank.events.impl.operations;

import com.lulobank.events.api.handler.Event;
import com.lulobank.events.api.operations.EventTemplate;
import io.vavr.concurrent.Future;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;

import static com.lulobank.events.api.utils.EventUtils.json;


@Slf4j
public class SqsTemplate implements EventTemplate {
    private final SqsAsyncClient sqsAsyncClient;
    public SqsTemplate(SqsAsyncClient sqsAsyncClient) {
        this.sqsAsyncClient = sqsAsyncClient;
    }

    @Override
    public <T> Future<Void> convertAndSend(String destinationName, Event<T> event, Map<String, Object> headers) {
        return send(destinationName, event, headers, null);
    }

    public <T> Future<Void> convertAndSend(String destinationName, Event<T> event) {
        return convertAndSend(destinationName, event, null);
    }

    @Override
    public <T> Future<Void> convertAndSend(String destinationName, Event<T> event, Map<String, Object> headers, int delay) {
        return send(destinationName, event, headerOf(headers, delay), null);
    }

    public <T> Future<Void> convertAndSend(String destinationName, Event<T> event, Map<String, Object> headers,
                                           int delay, Executor executor) {
        return send(destinationName, event, headerOf(headers, delay), executor);
    }

    private <T> Future<Void> send(String destinationName, Event<T> event, Map<String, Object> headers, Executor executor) {
        Executor defaultExecutor = executor == null ? ForkJoinPool.commonPool() : executor;
        return Future.run(defaultExecutor, () -> {
            log.info("Sending message to Queue {}, Payload: {}, Headers {}", destinationName, json(event), headers);
            sqsAsyncClient.sendMessage(builder -> builder.queueUrl(destinationName).messageBody(json(event)).build());
        });
    }

    private Map<String, Object> headerOf(Map<String, Object> headers, int delay) {
        Map<String, Object> result = headers == null ? new HashMap<>() : headers;
        result.put("delay", delay);
        return result;
    }


}
