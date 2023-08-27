package com.lulobank.events.api.receiver;

import io.vavr.control.Try;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * Interface to implement message receivers in reactive way
 * @author Carlos Duarte
 */
public interface MessageReceiver {
    /**
     * Executes a poll to receive messages from a queue
     *
     * @param messageHandler - Function that handle the message, it returns a Try<Void> to handle the success or failure of the message
     *
     */
    void executePoll(BiFunction<String, Map<String, String>, Try<Void>> messageHandler);

    /**
     * Receives messages from a queue
     * @return Flux of messages
     */
    Flux<List<Message>> receiveMessages();

    /**
     * Process a message
     * @param message - Message to process
     * @param messageHandler - Function that handle the message, it returns a Try<Void> to handle the success or failure of the message
     * @return Mono of void
     */
    Mono<Void> processMessage(Message message, BiFunction<String, Map<String, String>, Try<Void>> messageHandler);

    /**
     * Get the task scheduler defined in the message listener
     * @return Scheduler of the tasks
     */
    Scheduler getTaskScheduler();

    /**
     * Get the subscribe scheduler defined in the message listener
     * @return Scheduler of to subscribe
     */
    Scheduler getSubscribeScheduler();

}
