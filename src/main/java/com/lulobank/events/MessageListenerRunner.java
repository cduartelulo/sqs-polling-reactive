package com.lulobank.events;

import io.vavr.control.Either;
import io.vavr.control.Try;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class MessageListenerRunner implements ApplicationRunner {
   private final MessageListener messageListener;

    private static final Logger LOGGER = LoggerFactory.getLogger(com.lulobank.events.MessageListenerRunner.class);

    public MessageListenerRunner(MessageListener messageListener) {
        this.messageListener = messageListener;
    }


    @Override
    public void run(ApplicationArguments args) {
        messageListener
                .listen(5, message ->
                        {
                            Try.run(() -> Thread.sleep(new Random().nextInt(2000)));
                            LOGGER.debug("Message processed: {}", message);
                            return Either.right(null);
                        }
                );
    }
}

