package com.lulobank.events;

import io.vavr.control.Either;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.sqs.model.Message;

import java.util.List;
import java.util.function.Function;

public interface MessageReceiver {
    /**
     * Listen on a queue and process messages based on provided task definition
     *
     * @param eventHandler - describes how a message should be handled
     */
    void execute(Function<String, Either<?, Void>> eventHandler);

    Flux<List<Message>> receiveMessages();

    Mono<Void> processMessage(Message message, Function<String, Either<?, Void>> eventHandler);

}
