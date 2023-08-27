package com.lulobank.events.impl.listener;

import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import software.amazon.awssdk.arns.Arn;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlRequest;

import java.net.URI;
import java.net.URISyntaxException;

public class QueueUrlResolver {

    private final String queue;

    private final SqsClient sqsClient;

    public QueueUrlResolver(String queue, SqsClient sqsClient) {
        this.queue = queue;
        this.sqsClient = sqsClient;
    }

    public String resolveQueueUrl() {
        return isValidQueueUrl(this.queue)
                ? this.queue
                : doResolveQueueUrl();
    }

    private boolean isValidQueueUrl(String name) {
        try {
            URI candidate = new URI(name);
            return ("http".equals(candidate.getScheme()) || "https".equals(candidate.getScheme()));
        }
        catch (URISyntaxException e) {
            return false;
        }
    }

    private String doResolveQueueUrl() {
        GetQueueUrlRequest.Builder getQueueUrlRequestBuilder = GetQueueUrlRequest.builder();
        Arn arn = getQueueArnFromUrl();
        if (arn != null) {
            Assert.isTrue(arn.accountId().isPresent(), "accountId is missing from arn");
            getQueueUrlRequestBuilder.queueName(arn.resourceAsString()).queueOwnerAWSAccountId(arn.accountId().get());
        }
        else {
            getQueueUrlRequestBuilder.queueName(this.queue);
        }
        return this.sqsClient.getQueueUrl(getQueueUrlRequestBuilder.build()).queueUrl();
    }

    @Nullable
    private Arn getQueueArnFromUrl() {
        try {
            return Arn.fromString(this.queue);
        }
        catch (IllegalArgumentException e) {
            return null;
        }
    }
}
