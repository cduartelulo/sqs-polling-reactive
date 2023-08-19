package com.lulobank.events.config;

public class ReceiverProperties {
    private String queueURL;

    private String region;

    private int concurrency;

    public String getQueueURL() {
        return queueURL;
    }

    public void setQueueURL(String queueURL) {
        this.queueURL = queueURL;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public int getConcurrency() {
        return concurrency;
    }

    public void setConcurrency(int concurrency) {
        this.concurrency = concurrency;
    }
}