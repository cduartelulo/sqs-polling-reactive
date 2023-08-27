package com.lulobank.events.impl.config;


import com.lulobank.events.api.handler.EventHandler;
import com.lulobank.events.api.handler.EventProcessor;
import com.lulobank.events.impl.handler.DefaultEventProcessor;
import com.lulobank.events.api.handler.EventRegistry;
import com.lulobank.events.api.handler.SqsRegistry;
import com.lulobank.events.impl.listener.SqsListenerAnnotationBeanPostProcessor;
import com.lulobank.events.impl.receiver.SqsMessageReceiverBeanDefinitionRegistrar;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
public class MessageListenerConfig {

    @Bean
    public SqsListenerAnnotationBeanPostProcessor sqsListenerAnnotationBeanPostProcessor(@Autowired SqsMessageReceiverBeanDefinitionRegistrar registrarBean) {
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
    public EventProcessor eventProcessor(@Autowired EventRegistry eventRegistry) {
        return new DefaultEventProcessor(eventRegistry);
    }
}
