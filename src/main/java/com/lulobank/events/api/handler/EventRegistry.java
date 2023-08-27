package com.lulobank.events.api.handler;

import java.util.Map;

public interface EventRegistry {

    Map<String, EventHandler<?>> handlers();

}
