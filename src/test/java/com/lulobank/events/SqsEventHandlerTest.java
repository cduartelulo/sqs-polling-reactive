package com.lulobank.events;

import io.vavr.control.Either;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class SqsEventHandlerTest {

    /*@Test
    void contextLoads() {
    }*/

    @Autowired
    private MessageListener messageListener;

    @Test
    void testWhenHandlerNotHaveError() {
        messageListener.listen(5, message -> Either.right("Message handled successfully: " + message));
    }

    @Test
    void testWhenHanlerHaveError() {
        messageListener.listen(5, message -> Either.left("Error handling the message: " + message));
    }

}
