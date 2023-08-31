package com.lulobank.events.impl.listener;

import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Bean
public @interface EventMessage {

    @AliasFor(annotation = Bean.class)
    String name();

}
