package com.lulobank.events.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import software.amazon.awssdk.regions.Region;

@ConfigurationProperties(prefix = "aws.sqs")
public class SqsProperties {
        private String endpoint;
        private String region = Region.US_EAST_1.id();

        private String queueName;
        private String awsProfile = "digital-bank-developers";

        public String getEndpoint() {
                return endpoint;
        }

        public void setEndpoint(String endpoint) {
                this.endpoint = endpoint;
        }

        public String getRegion() {
                return region;
        }

        public void setRegion(String region) {
                this.region = region;
        }

        public String getQueueName() {
                return queueName;
        }

        public void setQueueName(String queueName) {
                this.queueName = queueName;
        }

        public String getAwsProfile() {
                return awsProfile;
        }

        public void setAwsProfile(String awsProfile) {
                this.awsProfile = awsProfile;
        }
}