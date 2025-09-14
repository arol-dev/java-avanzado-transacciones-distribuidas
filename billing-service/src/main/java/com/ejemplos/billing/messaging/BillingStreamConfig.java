package com.ejemplos.billing.messaging;

import com.ejemplos.common.events.SagaEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.function.Supplier;

@Configuration
public class BillingStreamConfig {

    private static final Logger log = LoggerFactory.getLogger(BillingStreamConfig.class);

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
        return () -> {
            SagaEvent ev = billingChargedQueue.poll();
            if (ev != null) {
                log.info("[BillingStream] Emisión de BILLING_CHARGED sagaId={} eventId={} ts={}", ev.sagaId(), ev.eventId(), ev.timestamp());
            }
            return ev;
        };
    }

    @Bean
    public Supplier<SagaEvent> sagaCompletedSupplier(@Qualifier("sagaCompletedQueue") BlockingQueue<SagaEvent> sagaCompletedQueue) {
        return () -> {
            SagaEvent ev = sagaCompletedQueue.poll();
            if (ev != null) {
                log.info("[BillingStream] Emisión de SAGA_COMPLETED sagaId={} eventId={} ts={}", ev.sagaId(), ev.eventId(), ev.timestamp());
            }
            return ev;
        };
    }
}
