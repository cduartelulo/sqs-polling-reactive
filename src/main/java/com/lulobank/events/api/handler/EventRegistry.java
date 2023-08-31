package com.lulobank.events.api.handler;

import java.util.Map;

public interface EventRegistry {

    @SuppressWarnings("java:S1452")
    Map<String, EventHandler<?>> handlers();

}
