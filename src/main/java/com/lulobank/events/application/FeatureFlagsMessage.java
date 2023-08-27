package com.lulobank.events.application;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FeatureFlagsMessage {

    private List<String> clients;
    private String featureFlag;

    public List<String> getClients() {
        return clients;
    }

    public void setClients(List<String> clients) {
        this.clients = clients;
    }

    public String getFeatureFlag() {
        return featureFlag;
    }

    public void setFeatureFlag(String featureFlag) {
        this.featureFlag = featureFlag;
    }

    public String toString() {
        return "FeatureFlagsEvent(clients=" + this.getClients() + ", featureFlag=" + this.getFeatureFlag() + ")";
    }
}
