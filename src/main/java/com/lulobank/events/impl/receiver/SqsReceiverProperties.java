package com.lulobank.events.impl.receiver;

import io.vavr.control.Option;

/**
 * Define the properties to configure the receiver of messages from the queue
 * @author Carlos Duarte
 */
public class SqsReceiverProperties {

    private final Option<Integer> maxMessagesPerPoll;

    private final Option<Integer> maxWaitTimeoutSecondsPerPoll;

    private final Option<Integer> retryDelaySeconds;

    private final Option<Integer> maxConcurrentMessages;

    private final Option<Integer> maxNumberOfThreads;

    private final Option<Integer> maxQueueCapacity;


    public SqsReceiverProperties(Builder builder) {
        this.maxMessagesPerPoll = builder.maxMessagesPerPoll;
        this.maxWaitTimeoutSecondsPerPoll = builder.maxWaitTimeoutSecondsPerPoll;
        this.retryDelaySeconds = builder.retryDelaySeconds;
        this.maxConcurrentMessages = builder.maxConcurrentMessages;
        this.maxNumberOfThreads = builder.maxNumberOfThreads;
        this.maxQueueCapacity = builder.maxQueueCapacity;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Option<Integer> getMaxMessagesPerPoll() {
        return maxMessagesPerPoll;
    }

    public Option<Integer> getMaxWaitTimeSecondsPerPoll() {
        return maxWaitTimeoutSecondsPerPoll;
    }

    public Option<Integer> getRetryDelaySeconds() {
        return retryDelaySeconds;
    }

    public Option<Integer> getMaxConcurrentMessages() {
        return maxConcurrentMessages;
    }

    public Option<Integer> getMaxNumberOfThreads() {
        return maxNumberOfThreads;
    }

    public Option<Integer> getMaxQueueCapacity() {
        return maxQueueCapacity;
    }

    public static class Builder {

        private Option<Integer> maxMessagesPerPoll;

        private Option<Integer> maxWaitTimeoutSecondsPerPoll;

        private Option<Integer> retryDelaySeconds;

        private Option<Integer> maxConcurrentMessages;

        private Option<Integer> maxNumberOfThreads;

        private Option<Integer> maxQueueCapacity;

        public Builder maxMessagesPerPoll(Option<Integer> maxMessagesPerPoll) {
            this.maxMessagesPerPoll = maxMessagesPerPoll;
            return this;
        }

        public Builder maxWaitTimeoutSecondsPerPoll(Option<Integer> maxWaitTimeoutSecondsPerPoll) {
            this.maxWaitTimeoutSecondsPerPoll = maxWaitTimeoutSecondsPerPoll;
            return this;
        }

        public Builder retryDelaySeconds(Option<Integer> retryDelaySeconds) {
            this.retryDelaySeconds = retryDelaySeconds;
            return this;
        }

        public Builder maxConcurrentMessages(Option<Integer> maxConcurrentMessages) {
            this.maxConcurrentMessages = maxConcurrentMessages;
            return this;
        }

        public Builder maxNumberOfThreads(Option<Integer> maxNumberOfThreads) {
            this.maxNumberOfThreads = maxNumberOfThreads;
            return this;
        }

        public Builder maxQueueCapacity(Option<Integer> maxQueueCapacity) {
            this.maxQueueCapacity = maxQueueCapacity;
            return this;
        }

        public SqsReceiverProperties build() {
            return new SqsReceiverProperties(this);
        }
    }

}
