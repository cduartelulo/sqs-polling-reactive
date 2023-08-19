package com.lulobank.events.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "cloud.aws")
public class AWSProperties {
    private String profile;

    private SQSProperties sqs;

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

    public SQSProperties getSqs() {
        return sqs;
    }

    public void setSqs(SQSProperties sqs) {
        this.sqs = sqs;
    }
}