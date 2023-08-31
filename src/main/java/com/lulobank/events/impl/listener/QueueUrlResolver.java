package com.lulobank.events.impl.listener;

import software.amazon.awssdk.arns.Arn;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlRequest;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;

/**
 * Resolve the queue url from the queue name
 *
 * @author Carlos Duarte
 */
public class QueueUrlResolver {

    private final String queue;

    private final SqsAsyncClient sqsClient;

    public QueueUrlResolver(String queue, SqsAsyncClient sqsClient) {
        this.queue = queue;
        this.sqsClient = sqsClient;
    }

    /**
     * Resolve the queue url from the queue name
     *
     * @return String the queue url
     */
    public String resolveQueueUrl() {
        return isValidQueueUrl(this.queue)
                ? this.queue
                : doResolveQueueUrl();
    }

    /**
     * Check if the queue name is a valid url
     *
     * @param name the queue name
     * @return boolean
     */
    private boolean isValidQueueUrl(String name) {
        try {
            URI candidate = new URI(name);
            return ("http".equals(candidate.getScheme()) || "https".equals(candidate.getScheme()));
        } catch (URISyntaxException e) {
            return false;
        }
    }

    /**
     * Resolve the queue url from the queue name. Uses the ARN to resolve it
     *
     * @return String the queue url
     */
    private String doResolveQueueUrl() {
        return this.sqsClient
                .getQueueUrl(
                        buildGetQueueUrlRequest(Arn.fromString(this.queue))
                ).join()
                .queueUrl();
    }

    private GetQueueUrlRequest buildGetQueueUrlRequest(Arn arn) {
        GetQueueUrlRequest.Builder getQueueUrlRequestBuilder = GetQueueUrlRequest.builder();
        if (!Objects.isNull(arn)) {
            String accountIdIsMissingFromArn = arn.accountId().orElseThrow(() -> new IllegalArgumentException("accountId is missing from arn"));
            getQueueUrlRequestBuilder.queueName(arn.resourceAsString()).queueOwnerAWSAccountId(accountIdIsMissingFromArn);
        } else {
            getQueueUrlRequestBuilder.queueName(this.queue);
        }
        return getQueueUrlRequestBuilder.build();
    }
}
