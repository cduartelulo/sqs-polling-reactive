package com.lulobank.events;

import io.vavr.control.Either;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class SqsMessageListenerTest extends AbstractIntegrationTest {

    @Autowired
    @Qualifier("sqsMessageListener1")
    private MessageListener messageListener;

    @Test
    void testWhenHandlerNotHaveError() {
        messageListener.listen(message -> Either.right(null));
    }

    @Test
    void testWhenHandlerHaveError() {
        messageListener.listen(message -> Either.left(new RuntimeException("Error handling the message: " + message)));
    }

}
