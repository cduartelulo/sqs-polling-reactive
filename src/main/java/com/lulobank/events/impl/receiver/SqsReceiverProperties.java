package com.lulobank.events.impl.receiver;

import io.vavr.control.Option;
import lombok.Builder;
import lombok.Getter;

/**
 * Define the properties to configure the receiver of messages from the queue
 * @author Carlos Duarte
 */
@Builder
@Getter
public class SqsReceiverProperties {

    private final Option<Integer> maxMessagesPerPoll;

    private final Option<Integer> maxWaitTimeSecondsPerPoll;

    private final Option<Integer> retryDelaySeconds;

    private final Option<Integer> maxConcurrentMessages;

    private final Option<Integer> maxNumberOfThreads;

    private final Option<Integer> maxQueueCapacity;

}
