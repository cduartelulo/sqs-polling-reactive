package com.lulobank.events;

import io.vavr.control.Either;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
@Component
public class MessageListenerRunner implements ApplicationRunner {
   private final EventHandler eventHandler;

    private static final Logger LOGGER = LoggerFactory.getLogger(com.lulobank.events.MessageListenerRunner.class);

    public MessageListenerRunner(EventHandler eventHandler) {
        this.eventHandler = eventHandler;
    }


    @Override
    public void run(ApplicationArguments args) {
//        eventHandler.handle(5, message -> Mono.fromRunnable(() -> {
//            LOGGER.info("Message handled successfully: {}", message);
//        }));
        eventHandler.handle(5, message -> Mono.fromSupplier(() -> {
            //return Either.left("Error handling the message: " + message);
            return Either.right("Message handled successfully: " + message);
        }));
    }
}

