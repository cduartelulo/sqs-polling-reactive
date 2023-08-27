package com.lulobank.events.application;

import com.lulobank.events.api.handler.EventHandler;
import io.vavr.control.Try;

public class FeatureFlagsMessageHandler implements EventHandler<FeatureFlagsMessage> {

    @Override
    public Try<Void> execute(FeatureFlagsMessage message) {
        return Try.success(null);
    }

    @Override
    public Class<FeatureFlagsMessage> eventClass() {
        return FeatureFlagsMessage.class;
    }
}
