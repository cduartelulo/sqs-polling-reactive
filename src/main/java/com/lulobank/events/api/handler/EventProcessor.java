package com.lulobank.events.api.handler;

import io.vavr.control.Either;
import io.vavr.control.Try;

import java.util.Map;


public interface EventProcessor {

    @SuppressWarnings("java:S1452")
    Either<MessageError, Void> handle(String event);

    Either<MessageError, Void> handle(String event, Map<String, String> attributes);

    Try<Void> handleTry(String event, Map<String, String> attributes);
}
