package com.lulobank.events.application;

import com.lulobank.events.api.handler.EventHandler;
import io.vavr.control.Try;

public class FeatureFlagsMessageHandler implements EventHandler<FeatureFlagsMessage> {

    @Override
    public Try<Void> execute(FeatureFlagsMessage message) {
        return Try.failure(new RuntimeException("Not implemented yet"));
    }

    @Override
    public Class<FeatureFlagsMessage> eventClass() {
        return FeatureFlagsMessage.class;
    }
}
