package com.lulobank.events.application;

import com.lulobank.events.api.handler.EventMessage;
import com.lulobank.events.api.handler.EventProcessor;
import com.lulobank.events.impl.config.MessagingConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.SqsClient;

import java.net.URI;

@Configuration
@Import(MessagingConfig.class)
public class ApplicationConfig {

    private static final String LOCALSTACK_URL = "http://localhost:4566";

    @Bean
    public SqsClient sqsSyncClient() {
        return SqsClient
                .builder()
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create("localstack", "localstack")))
                .endpointOverride(URI.create(LOCALSTACK_URL))
                .build();
    }

    @Bean
    public SqsAsyncClient sqsAsyncClient() {
        return SqsAsyncClient
                .builder()
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create("localstack", "localstack")))
                .endpointOverride(URI.create(LOCALSTACK_URL))
                .build();
    }

    @Bean
    public FeatureFlagsListener featureFlagsListener(EventProcessor eventProcessor) {
        return new FeatureFlagsListener(eventProcessor);
    }
    @EventMessage(name="FeatureFlagsEvent")
    public FeatureFlagsMessageHandler featureFlagsEventHandler() {
        return new FeatureFlagsMessageHandler();
    }
}
