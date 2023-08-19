package com.lulobank.events.config;

import com.lulobank.events.MessageReceiver;
import com.lulobank.events.SqsMessageReceiver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.SqsAsyncClientBuilder;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.SqsClientBuilder;

import java.net.URI;

@Configuration
public class MessagingConfig {

    @Bean
    public SqsClient sqsClient(AWSProperties awsProperties) {
        SqsClientBuilder sqsClientBuilder = SqsClient
                                                .builder()
                                                .credentialsProvider(ProfileCredentialsProvider.create(awsProperties.getProfile()));
        if (StringUtils.hasText(awsProperties.getSqs().getEndpoint())) {
            sqsClientBuilder.endpointOverride(URI.create(awsProperties.getSqs().getEndpoint()));
        }
        return sqsClientBuilder.build();
    }

    @Bean
    public SqsAsyncClient sqsAsyncClient(AWSProperties awsProperties) {
        SqsAsyncClientBuilder sqsAsyncClientBuilder = SqsAsyncClient
                                                        .builder()
                                                        .credentialsProvider(ProfileCredentialsProvider.create(awsProperties.getProfile()));

        if (StringUtils.hasText(awsProperties.getSqs().getEndpoint())) {
            sqsAsyncClientBuilder.endpointOverride(URI.create(awsProperties.getSqs().getEndpoint()));
        }

        return sqsAsyncClientBuilder.build();
    }

    @Bean
    public MessageReceiver messageReceiver1(SqsClient sqsClient, @Autowired AWSProperties awsProperties) {
        return new SqsMessageReceiver(sqsClient, awsProperties.getSqs().getReceiver1());
    }
}
