package com.lulobank.events.api.handler;

import io.vavr.control.Try;

import java.util.Map;

public interface EventHandler<T> {

    Try<Void> execute(T event);

    default Try<Void> execute(T event, Map<String, String> attributes) {
        return execute(event);
    }

    Class<T> eventClass();

    default Event<T> customListener(Event<T> event) {
        return event;
    }

    default void onError(T event) {
    }

}
