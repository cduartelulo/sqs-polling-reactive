package com.lulobank.events;

import com.lulobank.events.config.SqsProperties;
import io.vavr.control.Either;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@SpringBootApplication
@EnableConfigurationProperties(SqsProperties.class)
class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}

@Component
class MessageListenerRunner implements ApplicationRunner {

    private final EventHandler eventHandler;

    private static final Logger LOGGER = LoggerFactory.getLogger(MessageListenerRunner.class);

    public MessageListenerRunner(SqsEventHandler eventHandler) {
        this.eventHandler = eventHandler;
    }


    @Override
    public void run(ApplicationArguments args) {
//        eventHandler.handle(5, message -> Mono.fromRunnable(() -> {
//            LOGGER.info("Message handled successfully: {}", message);
//        }));
        eventHandler.handle(5, message -> Mono.fromSupplier(() -> {
            return Either.left("Error handling the message: " + message);
            //return Either.right("Message handled successfully: " + message);
        }));
    }
}
