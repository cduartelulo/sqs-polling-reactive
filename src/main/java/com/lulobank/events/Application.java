package com.lulobank.events;

import com.lulobank.events.config.AWSProperties;
import com.lulobank.events.config.YamlPropertySourceFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.PropertySource;

@SpringBootApplication
@EnableConfigurationProperties(AWSProperties.class)
@PropertySource(value = "classpath:sqsreceiver.yml", factory = YamlPropertySourceFactory.class)
class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}
