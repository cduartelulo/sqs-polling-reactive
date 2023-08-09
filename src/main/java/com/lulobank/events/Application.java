package com.lulobank.events;

import com.lulobank.events.config.SqsProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(SqsProperties.class)
class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}
