package com.lulobank.events.config;

public class SQSProperties {

    private ReceiverProperties receiver1;

    private ReceiverProperties receiver2;

    private String endpoint;

    public ReceiverProperties getReceiver1() {
        return receiver1;
    }

    public void setReceiver1(ReceiverProperties receiver1) {
        this.receiver1 = receiver1;
    }

    public ReceiverProperties getReceiver2() {
        return receiver2;
    }

    public void setReceiver2(ReceiverProperties receiver2) {
        this.receiver2 = receiver2;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }
}
