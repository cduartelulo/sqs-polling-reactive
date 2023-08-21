package com.lulobank.events.config;

import com.lulobank.events.MessageListener;
import com.lulobank.events.SqsMessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.utility.DockerImageName;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;

@Configuration
public class MessagingConfig {

    @Bean
    public LocalStackContainer localstack() {
        return new LocalStackContainer(DockerImageName.parse("localstack/localstack:2.2.0"))
                .withServices(LocalStackContainer.Service.SQS);
    }

    @Bean
    public SqsClient sqsClient(@Autowired LocalStackContainer localstack) {
        return SqsClient.builder()
                .endpointOverride(localstack.getEndpointOverride(LocalStackContainer.Service.SQS))
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(localstack.getAccessKey(), localstack.getSecretKey())))
                .region(Region.of(localstack.getRegion()))
                .build();
    }

    @Bean
    public MessageListener messageListener1(@Autowired SqsClient sqsClient, @Autowired SQSListenerProperties awsProperties) {
        return new SqsMessageListener(sqsClient, awsProperties.getSqs().getListeners().get(0));
    }
}
