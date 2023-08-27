package com.lulobank.events.api.receiver;

import java.util.Map;

/**
 * This class is a simplified version of the Message class from the AWS SDK for Java.
 * It is used to avoid a dependency on the AWS SDK for Java.
 * @author Carlos Duarte
 */
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
