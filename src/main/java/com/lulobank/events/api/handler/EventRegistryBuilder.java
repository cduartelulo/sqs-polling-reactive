package com.lulobank.events.api.handler;

import java.util.HashMap;
import java.util.Map;

public class EventRegistryBuilder {

    static Map<String, EventHandler<?>> handlers;

    private EventRegistryBuilder() {
    }

    public static EventRegistryBuilder newBuilder() {
        handlers = new HashMap<>();
        return new EventRegistryBuilder();
    }

    public EventRegistryBuilder onMessage(String eventPayload, EventHandler<?> handler) {
        handlers.put(eventPayload, handler);
        return this;
    }

    public Map<String, EventHandler<?>> build() {
        return handlers;
    }

}
