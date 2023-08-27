package com.lulobank.events.application;

import com.lulobank.events.api.handler.EventProcessor;
import com.lulobank.events.api.listener.SqsListener;
import io.vavr.control.Try;

import java.util.Map;

public class FeatureFlagsListener {

    private final EventProcessor eventProcessor;

    public FeatureFlagsListener(EventProcessor eventProcessor) {
        this.eventProcessor = eventProcessor;
    }

    @SqsListener(value = "${cloud.aws.sqs.listener1.queueUrl}", sqsClientBean = "sqsClient1", retryDelaySeconds = "5")
    public Try<Void> execute(String message, Map<String, String> attributes) {
        return eventProcessor.handleTry(message, attributes);
    }
}
