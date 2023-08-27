package com.lulobank.events.application;

import com.lulobank.events.api.handler.EventMessage;
import com.lulobank.events.api.handler.EventProcessor;
import com.lulobank.events.impl.config.MessageListenerConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.services.sqs.SqsClient;

import java.net.URI;

@Configuration
@Import(MessageListenerConfig.class)
public class ApplicationConfig {

    private static final String LOCALSTACK_URL = "http://localhost:4566";

    @Bean
    public SqsClient sqsClient1() {
        return SqsClient
                .builder()
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create("localstack", "localstack")))
                .endpointOverride(URI.create(LOCALSTACK_URL))
                .build();
    }

    @Bean
    public SqsClient sqsClient2() {
        return SqsClient
                .builder()
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create("localstack", "localstack")))
                .endpointOverride(URI.create(LOCALSTACK_URL))
                .build();
    }

    @Bean
    public FeatureFlagsListener featureFlagsListener(@Autowired EventProcessor eventProcessor) {
        return new FeatureFlagsListener(eventProcessor);
    }
    @EventMessage(name="FeatureFlagsEvent")
    public FeatureFlagsMessageHandler featureFlagsEventHandler() {
        return new FeatureFlagsMessageHandler();
    }
}
