package com.lulobank.events;

import io.vavr.control.Either;
import io.vavr.control.Try;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.Random;

//TODO: How to make it dynamically?
@Component
public class MessageListenerRunner implements ApplicationRunner {
    private static final Logger LOGGER = LoggerFactory.getLogger(MessageListenerRunner.class);
    private final MessageListener messageListener;

    public MessageListenerRunner(@Qualifier("sqsMessageListener1") MessageListener messageListener) {
        this.messageListener = messageListener;
    }

    @Override
    public void run(ApplicationArguments args) {
        messageListener
                .execute(message ->
                        {
                            Try.run(() -> Thread.sleep(new Random().nextInt(2000)));
                            LOGGER.debug("Message processed: {}", message);
                            return Either.right(null);
                        }
                );
    }
}

