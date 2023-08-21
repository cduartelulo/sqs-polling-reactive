package com.lulobank.events;

import org.junit.jupiter.api.BeforeAll;
import org.springframework.boot.test.context.SpringBootTest;
import software.amazon.awssdk.services.sqs.SqsClient;

@SpringBootTest
public abstract class AbstractIntegrationTest {

    private static SqsClient sqsClient;

    @BeforeAll
    public static void setupLocalStackContainer() {

    }

}
