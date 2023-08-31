package com.lulobank.events.impl.receiver;

import com.lulobank.events.api.utils.HashUtils;
import com.lulobank.events.impl.listener.QueueUrlResolver;
import com.lulobank.events.impl.listener.SqsListener;
import io.vavr.control.Try;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;

/**
 * Bean Factory to register the message receivers from annotations {@link SqsListener}
 * @author Carlos Duarte
 */
public class SqsMessageReceiverBeanDefinitionRegistrar implements BeanFactoryAware {

    private ConfigurableListableBeanFactory beanFactory;

    public SqsMessageReceiver registerBean(SqsAsyncClient sqsClient, String queue, SqsReceiverProperties sqsReceiverProperties) {
        String queueUrlResolved = new QueueUrlResolver(queue, sqsClient).resolveQueueUrl();
        SqsMessageReceiver sqsMessageReceiver = new SqsMessageReceiver(
                sqsClient,
                queueUrlResolved,
                sqsReceiverProperties);
        String beanName = getBeanName(queueUrlResolved);
        beanFactory.initializeBean(sqsMessageReceiver, beanName);
        beanFactory.registerSingleton(beanName, sqsMessageReceiver);
        return sqsMessageReceiver;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = (ConfigurableListableBeanFactory) beanFactory;
    }

    @SuppressWarnings("java:S1612")
    private String getBeanName(String queueUrl) {
        return Try.of(() -> "sqsMessageReceiver" + HashUtils.calculateSHA256Hash(queueUrl)).getOrElseThrow(e -> new RuntimeException(e));
    }

}
