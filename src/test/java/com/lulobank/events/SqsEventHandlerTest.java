package com.lulobank.events;

import io.vavr.control.Either;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Mono;

@SpringBootTest
class SqsEventHandlerTest {

    /*@Test
    void contextLoads() {
    }*/

    @Autowired
    private EventHandler eventHandler;

    @Test
    void testWhenHandlerNotHaveError() {
        eventHandler.handle(5, message -> Mono.fromSupplier(() -> Either.right("Message handled successfully: " + message)));
    }

    @Test
    void testWhenHanlerHaveError() {
        eventHandler.handle(5, message -> Mono.fromSupplier(() -> Either.left("Error handling the message: " + message)));
    }

}
