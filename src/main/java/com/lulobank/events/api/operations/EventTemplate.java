package com.lulobank.events.api.operations;

import com.lulobank.events.api.handler.Event;
import com.lulobank.events.api.handler.EventFactory;
import io.vavr.concurrent.Future;

import java.util.Map;

public interface EventTemplate {

    <T> Future<Void> convertAndSend(String destinationName, Event<T> event, Map<String, Object> headers);

    <T> Future<Void> convertAndSend(String destinationName, Event<T> event, Map<String, Object> headers, int delay);

    default <T> Future<Void> convertAndSend(String destinationName, T payload, Map<String, Object> headers) {
        return convertAndSend(destinationName, EventFactory.ofDefaults(payload).build(), headers);
    }

}
