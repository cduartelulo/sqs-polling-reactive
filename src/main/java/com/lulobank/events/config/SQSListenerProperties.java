package com.lulobank.events.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "cloud.aws")
public class SQSListenerProperties {
    private SQS sqs;

    public SQS getSqs() {
        return sqs;
    }

    public void setSqs(SQS sqs) {
        this.sqs = sqs;
    }

    public static class SQS {
        private List<Listener> listeners;

        private String endpoint;

        public List<Listener> getListeners() {
            return listeners;
        }

        public void setListeners(List<Listener> listeners) {
            this.listeners = listeners;
        }

        public String getEndpoint() {
            return endpoint;
        }

        public void setEndpoint(String endpoint) {
            this.endpoint = endpoint;
        }

        public static class Listener {

            private String name;
            private String queueURL;

            private String region;

            private int maxNumberOfMessages;

            private int waitTimeSeconds;

            private int visibilityTimeout;

            private int maximumNumberOfThreads;

            private int concurrency;

            private int maximumQueueCapacity;

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }

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

            public int getMaxNumberOfMessages() {
                return maxNumberOfMessages;
            }

            public void setMaxNumberOfMessages(int maxNumberOfMessages) {
                this.maxNumberOfMessages = maxNumberOfMessages;
            }

            public int getWaitTimeSeconds() {
                return waitTimeSeconds;
            }

            public void setWaitTimeSeconds(int waitTimeSeconds) {
                this.waitTimeSeconds = waitTimeSeconds;
            }

            public int getVisibilityTimeout() {
                return visibilityTimeout;
            }

            public void setVisibilityTimeout(int visibilityTimeout) {
                this.visibilityTimeout = visibilityTimeout;
            }

            public int getMaximumNumberOfThreads() {
                return maximumNumberOfThreads;
            }

            public void setMaximumNumberOfThreads(int maximumNumberOfThreads) {
                this.maximumNumberOfThreads = maximumNumberOfThreads;
            }

            public int getConcurrency() {
                return concurrency;
            }

            public void setConcurrency(int concurrency) {
                this.concurrency = concurrency;
            }

            public int getMaximumQueueCapacity() {
                return maximumQueueCapacity;
            }

            public void setMaximumQueueCapacity(int maximumQueueCapacity) {
                this.maximumQueueCapacity = maximumQueueCapacity;
            }
        }
    }
}