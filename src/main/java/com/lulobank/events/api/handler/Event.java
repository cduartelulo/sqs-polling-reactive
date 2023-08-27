package com.lulobank.events.api.handler;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Event<T> {

    private String id;
    private String eventType;
    private T payload;
    private int receiveCount;
    private int maximumReceives;
    private int delay;

}
