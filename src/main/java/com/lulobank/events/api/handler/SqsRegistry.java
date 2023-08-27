package com.lulobank.events.api.handler;

import java.util.Map;

public class SqsRegistry implements EventRegistry {

    private final Map<String, EventHandler<?>> handlers;

    public SqsRegistry(Map<String, EventHandler<?>> handlers) {
        this.handlers = handlers;
    }
    @Override
    public Map<String, EventHandler<?>> handlers() {
        return this.handlers;
    }
}
