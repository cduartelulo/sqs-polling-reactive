package com.lulobank.events.api.handler;

import io.vavr.control.Try;

import java.util.Map;

public interface EventHandler<T> {

    Try<Void> execute(T event);

    Class<T> eventClass();

    default Event<T> customListener(Event<T> event) {
        return event;
    }

    default void onError(T event) {
    }

}
