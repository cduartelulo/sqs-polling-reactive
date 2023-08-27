package com.lulobank.events.api.receiver;

import java.util.Map;

public class Message {

    private final String messageId;
    private final String body;

    private final Map<String, String> attributes;

    private final String receiptHandle;

    public Message(String messageId, String body, Map<String, String> attributes, String receiptHandle) {
        this.messageId = messageId;
        this.body = body;
        this.attributes = attributes;
        this.receiptHandle = receiptHandle;
    }

    public String getMessageId() {
        return messageId;
    }

    public String getBody() {
        return body;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public String getReceiptHandle() {
        return receiptHandle;
    }

    public String toString() {
        return "Message(messageId=" + this.getMessageId() + ", body=" + this.getBody() + ", attributes=" + this.getAttributes() + ", receiptHandle=" + this.getReceiptHandle() + ")";
    }
}
