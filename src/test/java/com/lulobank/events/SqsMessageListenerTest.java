package com.lulobank.events;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

class SqsMessageListenerTest extends AbstractIntegrationTest {

    @Test
    void testWhenHandlerNotHaveError() {
        //messageListener.listen((message, attributes) -> Either.right(null));
    }

    @Test
    void testWhenHandlerHaveError() {
        //messageListener.listen((message, attributes) -> Either.left(new RuntimeException("Error handling the message: " + message)));
    }

}
