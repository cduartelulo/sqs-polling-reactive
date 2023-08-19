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
public class MessageReceiverRunner implements ApplicationRunner {
   private final MessageReceiver messageReceiver;

    private static final Logger LOGGER = LoggerFactory.getLogger(MessageReceiverRunner.class);

    public MessageReceiverRunner(MessageReceiver messageReceiver) {
        this.messageReceiver = messageReceiver;
    }


    @Override
    public void run(ApplicationArguments args) {
        messageReceiver
                .execute(message ->
                        {
                            Try.run(() -> Thread.sleep(new Random().nextInt(2000)));
                            LOGGER.debug("Message processed: {}", message);
                            return Either.right(null);
                        }
                );
    }
}

