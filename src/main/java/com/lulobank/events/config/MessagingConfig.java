package com.lulobank.events.config;

import com.lulobank.events.SqsEventHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.SqsAsyncClientBuilder;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.SqsClientBuilder;

import java.net.URI;

@Configuration
public class MessagingConfig {

    @Bean
    public SqsClient sqsClient(SqsProperties sqsProperties) {
        SqsClientBuilder sqsClientBuilder = SqsClient
                                                .builder()
                                                .region(Region.of(sqsProperties.getRegion()))
                                                .credentialsProvider(ProfileCredentialsProvider.create(sqsProperties.getAwsProfile()));
        if (StringUtils.hasText(sqsProperties.getEndpoint())) {
            sqsClientBuilder.endpointOverride(URI.create(sqsProperties.getEndpoint()));
        }
        return sqsClientBuilder.build();
    }

    @Bean
    public SqsAsyncClient sqsAsyncClient(SqsProperties sqsProperties) {
        SqsAsyncClientBuilder sqsAsyncClientBuilder = SqsAsyncClient
                                                        .builder()
                                                        .region(Region.of(sqsProperties.getRegion()))
                                                        .credentialsProvider(ProfileCredentialsProvider.create(sqsProperties.getAwsProfile()));

        if (StringUtils.hasText(sqsProperties.getEndpoint())) {
            sqsAsyncClientBuilder.endpointOverride(URI.create(sqsProperties.getEndpoint()));
        }

        return sqsAsyncClientBuilder.build();
    }

    @Bean
    public SqsEventHandler eventHandler(SqsClient sqsClient) {
        return new SqsEventHandler(sqsClient);
    }
}
