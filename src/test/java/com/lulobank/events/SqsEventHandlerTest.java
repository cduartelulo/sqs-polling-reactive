package com.lulobank.events;

import io.vavr.control.Either;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class SqsEventHandlerTest {

    @Autowired
    private MessageReceiver messageReceiver;

    @Test
    void testWhenHandlerNotHaveError() {
        messageReceiver.execute(message -> Either.right(null));
    }

    @Test
    void testWhenHandlerHaveError() {
        messageReceiver.execute(message -> Either.left(new RuntimeException("Error handling the message: " + message)));
    }

}
