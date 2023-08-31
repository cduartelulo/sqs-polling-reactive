package com.lulobank.events.api.handler;

import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

@Slf4j
public class EventFactory<T> {

    private String id;
    private String eventType;
    private final T payload;
    public EventFactory(T payload) {
        this.payload = payload;
    }

    public static <R> EventFactory<R> ofDefaults(R payload) {
        EventFactory<R> def = new EventFactory<>(payload);
        def.id = UUID.randomUUID().toString();
        def.eventType = payload.getClass().getSimpleName();
        return def;
    }

    public EventFactory<T> id(String id) {
        this.id = id;
        return this;
    }

    public EventFactory<T> eventType(String eventType) {
        this.eventType = eventType;
        return this;
    }

    public Event<T> build() {
        return new Event<>(this.id,
                this.eventType,
                this.payload);
    }


}
