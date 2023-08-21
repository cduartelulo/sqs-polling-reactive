package com.lulobank.events.config;

import com.lulobank.events.MessageListener;
import com.lulobank.events.SqsMessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.SqsAsyncClientBuilder;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.SqsClientBuilder;

import java.net.URI;
import java.util.List;
import java.util.Optional;

@Configuration
public class MessagingConfig {

    public static final String LISTENER_1 = "listener1";

    public static final String LISTENER_2 = "listener2";

    @Bean
    public SqsClient sqsClient(SQSListenerProperties awsProperties) {
        SqsClientBuilder sqsClientBuilder = SqsClient
                .builder()
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create("localstack", "localstack")));

        if (StringUtils.hasText(awsProperties.getSqs().getEndpoint())) {
            sqsClientBuilder.endpointOverride(URI.create(awsProperties.getSqs().getEndpoint()));
        }
        return sqsClientBuilder.build();
    }

    @Bean
    public SqsAsyncClient sqsAsyncClient(SQSListenerProperties awsProperties) {
        SqsAsyncClientBuilder sqsAsyncClientBuilder = SqsAsyncClient
                .builder()
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create("localstack", "localstack")));

        if (StringUtils.hasText(awsProperties.getSqs().getEndpoint())) {
            sqsAsyncClientBuilder.endpointOverride(URI.create(awsProperties.getSqs().getEndpoint()));
        }

        return sqsAsyncClientBuilder.build();
    }

    @Bean
    public MessageListener sqsMessageListener1(SqsClient sqsClient, @Autowired SQSListenerProperties sqsListenerProperties) {
        return new SqsMessageListener(sqsClient, filterListener(sqsListenerProperties.getSqs().getListeners(), LISTENER_1));
    }

    @Bean
    public MessageListener sqsMessageListener2(SqsClient sqsClient, @Autowired SQSListenerProperties sqsListenerProperties) {
        return new SqsMessageListener(sqsClient, filterListener(sqsListenerProperties.getSqs().getListeners(), LISTENER_2));
    }

    private SQSListenerProperties.SQS.Listener filterListener(List<SQSListenerProperties.SQS.Listener> listeners, String listenerName) {
        Optional<SQSListenerProperties.SQS.Listener> optionalListener = listeners.stream().filter(listener -> listener.getName().equals(listenerName)).findFirst();

        if (optionalListener.isPresent()) {
            return optionalListener.get();
        } else {
            throw new RuntimeException("Listener not found");
        }
    }
}
