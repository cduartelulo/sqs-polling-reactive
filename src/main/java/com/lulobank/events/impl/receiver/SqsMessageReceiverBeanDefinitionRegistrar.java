package com.lulobank.events.impl.receiver;

import com.lulobank.events.impl.listener.QueueUrlResolver;
import com.lulobank.events.impl.listener.SqsListenerProperties;
import com.lulobank.events.impl.utils.HashUtils;
import io.vavr.control.Try;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.lang.NonNull;
import software.amazon.awssdk.services.sqs.SqsClient;

public class SqsMessageReceiverBeanDefinitionRegistrar implements BeanFactoryAware {

    private ConfigurableListableBeanFactory beanFactory;

    public SqsMessageReceiver registerBean(SqsClient sqsClient, String queue, SqsListenerProperties sqsMessageReceiverProperties) {
        String queueUrlResolved = new QueueUrlResolver(queue, sqsClient).resolveQueueUrl();
        SqsMessageReceiver sqsMessageReceiver = new SqsMessageReceiver(
                sqsClient,
                queueUrlResolved,
                sqsMessageReceiverProperties);
        String beanName = getBeanName(queueUrlResolved);
        beanFactory.initializeBean(sqsMessageReceiver, beanName);
        beanFactory.registerSingleton(beanName, sqsMessageReceiver);
        return sqsMessageReceiver;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = (ConfigurableListableBeanFactory) beanFactory;
    }

    private String getBeanName(String queueUrl) {
        return Try.of(() ->  "sqsMessageReceiver" + HashUtils.calculateSHA256Hash(queueUrl)).getOrElseThrow((e) -> new RuntimeException(e));
    }

}
