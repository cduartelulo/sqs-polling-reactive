package com.lulobank.events;

import io.vavr.control.Either;

import java.util.function.Function;

public interface MessageListener {
    /**
     * Listen on a queue and process messages based on provided task definition
     *
     * @param concurrency - level of parallelization
     * @param task - describes how a message should be handled
     */
    void listen(int concurrency, Function<String, Either<String, String>> task);
}
