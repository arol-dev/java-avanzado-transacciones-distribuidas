package com.ejemplos.billing.messaging;

import com.ejemplos.common.events.SagaEvent;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.function.Supplier;

@Configuration
public class BillingStreamConfig {

    @Bean
    public BlockingQueue<SagaEvent> billingChargedQueue() {
        return new ArrayBlockingQueue<>(1024);
    }

    @Bean
    public BlockingQueue<SagaEvent> sagaCompletedQueue() {
        return new ArrayBlockingQueue<>(1024);
    }

    @Bean
    public Supplier<SagaEvent> billingChargedSupplier(@Qualifier("billingChargedQueue") BlockingQueue<SagaEvent> billingChargedQueue) {
        return billingChargedQueue::poll;
    }

    @Bean
    public Supplier<SagaEvent> sagaCompletedSupplier(@Qualifier("sagaCompletedQueue") BlockingQueue<SagaEvent> sagaCompletedQueue) {
        return sagaCompletedQueue::poll;
    }
}
