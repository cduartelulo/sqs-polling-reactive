package com.lulobank.events;

import reactor.core.publisher.Mono;

import java.util.function.Function;

public interface EventHandler {
    /**
     * Listen on a queue and process messages based on provided task definition
     *
     * @param concurrency - level of parallelization
     * @param task - describes how a message should be handled
     */
    void handle(int concurrency, Function<String, Mono<Void>> task);
}
