package com.lulobank.events.impl.listener;

import com.lulobank.events.api.listener.SqsListener;
import com.lulobank.events.api.receiver.MessageReceiver;
import com.lulobank.events.impl.receiver.SqsMessageReceiverBeanDefinitionRegistrar;
import com.lulobank.events.impl.receiver.SqsReceiverProperties;
import io.vavr.control.Option;
import io.vavr.control.Try;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.*;
import org.springframework.core.MethodIntrospector;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.services.sqs.SqsClient;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Bean post processor to register the message listeners from the annotations {@link SqsListener}
 * @author Carlos Duarte
 */
public class SqsListenerAnnotationBeanPostProcessor implements BeanPostProcessor, BeanFactoryAware {

    private final Collection<Class<?>> nonAnnotatedClasses = Collections.synchronizedSet(new HashSet<>());
    private final SqsMessageReceiverBeanDefinitionRegistrar registrarBean;
    private BeanFactory beanFactory;
    @Nullable
    private BeanExpressionResolver expressionResolver;
    @Nullable
    private BeanExpressionContext expressionContext;

    public SqsListenerAnnotationBeanPostProcessor(SqsMessageReceiverBeanDefinitionRegistrar registrarBean) {
        this.registrarBean = registrarBean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Class<?> targetClass = AopUtils.getTargetClass(bean);
        if (this.nonAnnotatedClasses.contains(targetClass)) {
            return bean;
        }
        detectAnnotationsAndRegisterMessageListeners(bean, targetClass);
        return bean;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    protected void detectAnnotationsAndRegisterMessageListeners(Object bean, Class<?> targetClass) {
        Map<Method, SqsListener> annotatedMethods = MethodIntrospector.selectMethods(targetClass,
                (MethodIntrospector.MetadataLookup<SqsListener>) method -> AnnotatedElementUtils.findMergedAnnotation(method,
                        SqsListener.class));
        if (annotatedMethods.isEmpty()) {
            this.nonAnnotatedClasses.add(targetClass);
        }

        annotatedMethods.entrySet().stream()
                .map(entry -> createAndConfigureMessageListener(entry.getValue(), entry.getKey()))
                .forEach(messageListenerContainer -> executeReceiver(messageListenerContainer.getMessageListener(), bean, messageListenerContainer.getMethod()));
    }

    protected MessageListenerContainer createAndConfigureMessageListener(SqsListener annotation, Method method) {

        SqsReceiverProperties sqsReceiverProperties = SqsReceiverProperties.builder()
                .maxMessagesPerPoll(Option.of(resolveAsInteger(annotation.maxMessagesPerPoll(), "maxMessagesPerPoll")))
                .maxWaitTimeoutSecondsPerPoll(Option.of(resolveAsInteger(annotation.maxWaitTimeSecondsPerPoll(), "maxWaitTimeoutSecondsPerPoll")))
                .retryDelaySeconds(Option.of(resolveAsInteger(annotation.retryDelaySeconds(), "retryDelaySeconds")))
                .maxConcurrentMessages(Option.of(resolveAsInteger(annotation.maxConcurrentMessages(), "maxConcurrentMessages")))
                .maxNumberOfThreads(Option.of(resolveAsInteger(annotation.maxNumberOfThreads(), "maxNumberOfThreads")))
                .maxQueueCapacity(Option.of(resolveAsInteger(annotation.maxQueueCapacity(), "maxQueueCapacity")))
                .build();
        SqsClient sqsClient = beanFactory.getBean(annotation.sqsClientBean(), SqsClient.class);
        MessageReceiver messageReceiver = registrarBean.registerBean(sqsClient, resolveAsString(annotation.value(), "queue"), sqsReceiverProperties);
        return new MessageListenerContainer(messageReceiver, method);
    }


    @SuppressWarnings("unchecked")
    protected void executeReceiver(MessageReceiver messageReceiver, Object bean, Method method) {
        messageReceiver.executePoll((message, attributes) -> {
            Try<Void> result;
            try {
                result = (Try<Void>) method.invoke(bean, message, attributes);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
            return result;
        });
    }

    @Nullable
    protected BeanExpressionResolver getExpressionResolver() {
        if (this.expressionResolver == null && this.beanFactory instanceof ConfigurableListableBeanFactory) {
            ConfigurableListableBeanFactory clbf = (ConfigurableListableBeanFactory) this.beanFactory;
            this.expressionResolver = clbf.getBeanExpressionResolver();
        }
        return this.expressionResolver;
    }

    @Nullable
    protected Object resolveExpression(String value) {
        return getExpressionResolver() != null
                ? getExpressionResolver().evaluate(resolve(value), Objects.requireNonNull(getExpressionContext()))
                : value;
    }

    @Nullable
    protected BeanExpressionContext getExpressionContext() {
        if (this.expressionContext == null && this.beanFactory instanceof ConfigurableBeanFactory) {
            ConfigurableBeanFactory clbf = (ConfigurableBeanFactory) this.beanFactory;
            this.expressionContext = new BeanExpressionContext(clbf, null);
        }
        return this.expressionContext;
    }

    @Nullable
    protected String resolve(String value) {
        if (this.beanFactory instanceof ConfigurableBeanFactory) {
            ConfigurableBeanFactory cbf = (ConfigurableBeanFactory) this.beanFactory;
            return cbf.resolveEmbeddedValue(value);
        }
        return value;
    }

    protected String resolveAsString(String value, String propertyName) {
        try {
            Collection<String> resolvedStrings = resolveAsStrings(resolve(value));
            return resolvedStrings.isEmpty() ? value : resolvedStrings.iterator().next();
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not resolve property " + propertyName, e);
        }
    }

    protected Collection<String> resolveAsStrings(@Nullable Object resolvedValue) {
        if (resolvedValue instanceof String[]) {
            String[] strArr = (String[]) resolvedValue;
            return resolveFromStream(Arrays.stream(strArr));
        } else if (resolvedValue instanceof Iterable<?>) {
            Iterable<?> itr = (Iterable<?>) resolvedValue;
            return resolveFromStream(StreamSupport.stream(itr.spliterator(), false));
        } else if (resolvedValue instanceof String) {
            String str = (String) resolvedValue;
            return Collections.singletonList(str);
        } else {
            throw new IllegalArgumentException("Cannot resolve " + resolvedValue + " as String");
        }
    }

    protected List<String> resolveFromStream(Stream<?> stream) {
        return stream.flatMap(str -> resolveAsStrings(str).stream()).collect(Collectors.toList());
    }

    @Nullable
    protected Integer resolveAsInteger(String value, String propertyName) {
        try {
            Object resolvedValue = resolveExpression(value);
            if (resolvedValue instanceof Number) {
                Number numberValue = (Number) resolvedValue;
                return numberValue.intValue();
            } else if (resolvedValue instanceof String) {
                String stringValue = (String) resolvedValue;
                return StringUtils.hasText(stringValue) ? Integer.parseInt(stringValue) : null;
            }
            return null;
        } catch (Exception e) {
            throw new IllegalArgumentException("Cannot resolve " + propertyName + " as Integer");
        }
    }

    public static class MessageListenerContainer {

        private final MessageReceiver messageListener;

        private final Method method;

        public MessageListenerContainer(MessageReceiver messageListener, Method method) {
            this.messageListener = messageListener;
            this.method = method;
        }

        public MessageReceiver getMessageListener() {
            return messageListener;
        }

        public Method getMethod() {
            return method;
        }

    }

}
