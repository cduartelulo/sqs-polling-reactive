package com.lulobank.events.api.handler;

import io.vavr.control.Either;
import io.vavr.control.Try;

import java.util.Map;


public interface EventProcessor {

    Either<?, Void> handle(String event);

    Either<?, Void> handle(String event, Map<String, String> attributes);

    Try<Void> handleTry(String event, Map<String, String> attributes);
}
