package com.lulobank.events.impl.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.vavr.control.Try;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EventUtils {

    private EventUtils() {
    }

    public static String json(Object e) {
        return Try.of(() -> {
            ObjectMapper om = new ObjectMapper();
            om.registerModule(new JavaTimeModule());
            return om.writeValueAsString(e);
        }).onFailure(ex -> log.error("Error reading object " + e, ex))
                .getOrElse("");
    }

}
