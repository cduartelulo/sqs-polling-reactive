package com.lulobank.events.impl.config;


import com.lulobank.events.api.handler.EventHandler;
import com.lulobank.events.api.handler.EventProcessor;
import com.lulobank.events.impl.handler.DefaultEventProcessor;
import com.lulobank.events.api.handler.EventRegistry;
import com.lulobank.events.impl.handler.SqsRegistry;
import com.lulobank.events.impl.listener.SqsListenerAnnotationBeanPostProcessor;
import com.lulobank.events.impl.receiver.SqsMessageReceiverBeanDefinitionRegistrar;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

/**
 * Spring configuration class to configure the MessageListener. Use @Import to import this configuration.
 * See {@link org.springframework.context.annotation.Import} for more information.
 *
 * @author Carlos Duarte
 */
@Configuration
public class MessagingConfig {

    @Bean
    public SqsListenerAnnotationBeanPostProcessor sqsListenerAnnotationBeanPostProcessor(SqsMessageReceiverBeanDefinitionRegistrar registrarBean) {
        return new SqsListenerAnnotationBeanPostProcessor(registrarBean);
    }

    @Bean
    public SqsMessageReceiverBeanDefinitionRegistrar sqsMessageListenerBeanDefinitionRegistrar() {
        return new SqsMessageReceiverBeanDefinitionRegistrar();
    }
    @Bean
    public EventRegistry eventRegistry(Map<String, EventHandler<?>> handlers) {
        return new SqsRegistry(handlers);
    }

    @Bean
    public EventProcessor eventProcessor(EventRegistry eventRegistry) {
        return new DefaultEventProcessor(eventRegistry);
    }
}
