package com.lulobank.events.api.receiver;

import io.vavr.control.Try;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

public interface MessageReceiver {
    /**
     * Listen on a queue and process messages based on provided task definition
     *
     * @param messageHandler - describes how a message should be handled
     */
    void executePoll(BiFunction<String, Map<String, String>, Try<Void>> messageHandler);

    Flux<List<Message>> receiveMessages();

    Mono<Void> processMessage(Message message, BiFunction<String, Map<String, String>, Try<Void>> messageHandler);

    Scheduler getTaskScheduler();

    Scheduler getSubscribeScheduler();

}
