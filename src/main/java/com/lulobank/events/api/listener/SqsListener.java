/*
 * Copyright 2013-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.lulobank.events.api.listener;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SqsListener {

	/**
	 * Array of queue names or urls. Queues declared in the same annotation will be handled by the same
	 * @return queue name or url.
	 */
	String value() default "";

	/**
	 * Alias for {@link #value()}
	 * @return queue name or url.
	 */
	@AliasFor("value")
	String queueName() default "";

	/**
	 * Spring bean name to use for retrieving a SQS client.
	 * @return String
	 */
	String sqsClientBean();

	/**
	 * The maximum number of messages to receive in a single poll to the queue. Defaults to 10.
	 * Max is 10.
	 * @return max messages per poll.
	 */
	String maxMessagesPerPoll() default "";

	/**
	 * The duration in seconds for which the receive message call waits for a message to arrive in the queue before returning. Defaults to 20.
	 * @return String
	 */
	String maxWaitTimeSecondsPerPoll() default "";

	/**
	 * The duration in seconds for which the message is delayed when a retry is required.
	 * @return String
	 */
	String retryDelaySeconds() default "";

	/**
	 * The maximum number of messages to process concurrently. Defaults to the number of available processors.
	 * @return String
	 */
	String maxConcurrentMessages() default "";

	/**
	 * The maximum number of threads in the task scheduler. Defaults to 10 * the number of available processors.
	 * @return String
	 */

	String maxNumberOfThreads() default "";

	/**
	 * The maximum of enqueued tasks PER THREAD in the task scheduler. Defaults to 100000.
	 * @return String
	 */
	String maxQueueCapacity() default "";

}
