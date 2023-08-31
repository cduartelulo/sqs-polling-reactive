package com.lulobank.events.api.handler;

import lombok.Getter;

@Getter
public class MessageError {
    private final String cause;

    public MessageError(String cause) {
        this.cause = cause;
    }
}
