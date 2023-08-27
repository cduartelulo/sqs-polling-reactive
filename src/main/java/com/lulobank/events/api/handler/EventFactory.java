package com.lulobank.events.api.handler;

import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

@Slf4j
public class EventFactory<T> {

    private String id;
    private String eventType;
    private final T payload;
    private int maximumReceives;
    private int delay;
    public static final int DEFAULT_MAX_RECEIVE = 5;
    public static final int DEFAULT_DELAY = 5;
    public static final int DEFAULT_RECEIVE_INIT = 0;

    public EventFactory(T payload) {
        this.payload = payload;
    }

    public static <R> EventFactory<R> ofDefaults(R payload) {
        EventFactory<R> def = new EventFactory<>(payload);
        def.id = UUID.randomUUID().toString();
        def.maximumReceives = DEFAULT_MAX_RECEIVE;
        def.delay = DEFAULT_DELAY;
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

    public EventFactory<T> maximumReceives(int maximumReceives) {
        this.maximumReceives = maximumReceives;
        return this;
    }

    public EventFactory<T> delay(int delay) {
        this.delay = delay;
        return this;
    }

    public Event<T> build() {
        return new Event<>(this.id,
                this.eventType,
                this.payload,
                DEFAULT_RECEIVE_INIT,
                this.maximumReceives,
                this.delay);
    }


}
