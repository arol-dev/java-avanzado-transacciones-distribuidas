package com.ejemplos.billing.messaging;

import com.ejemplos.common.dto.BillingCharge;
import com.ejemplos.common.events.SagaEvent;
import com.ejemplos.common.events.SagaEventType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.function.Consumer;

@Configuration
public class BillingChoreographyListener {

    private static final Logger log = LoggerFactory.getLogger(BillingChoreographyListener.class);

    private final BlockingQueue<SagaEvent> billingChargedQueue;
    private final BlockingQueue<SagaEvent> sagaCompletedQueue;

    private final Map<String, Boolean> flightOk = new HashMap<>();
    private final Map<String, Boolean> hotelOk = new HashMap<>();

    public BillingChoreographyListener(BlockingQueue<SagaEvent> billingChargedQueue,
                                       BlockingQueue<SagaEvent> sagaCompletedQueue) {
        this.billingChargedQueue = billingChargedQueue;
        this.sagaCompletedQueue = sagaCompletedQueue;
    }

    @Bean
    public Consumer<SagaEvent> flightReservedConsumer() {
        return event -> {
            if (event == null || event.type() != SagaEventType.FLIGHT_RESERVED)
                return;
            log.info("Vuelo reservado para sagaId={}", event.sagaId());
            flightOk.put(event.sagaId(), true);
            tryComplete(event.sagaId());
        };
    }

    @Bean
    public Consumer<SagaEvent> hotelReservedConsumer() {
        return event -> {
            if (event == null || event.type() != SagaEventType.HOTEL_RESERVED)
                return;
            log.info("Hotel reservado para sagaId={}", event.sagaId());
            hotelOk.put(event.sagaId(), true);
            tryComplete(event.sagaId());
        };
    }

    private void tryComplete(String sagaId) {
        if (Boolean.TRUE.equals(flightOk.get(sagaId)) && Boolean.TRUE.equals(hotelOk.get(sagaId))) {
            log.info("Ambas reservas listas, cobrando sagaId={}", sagaId);
            BillingCharge charge = new BillingCharge(
                    UUID.randomUUID().toString(),
                    "unknown",
                    0,
                    true,
                    null
            );
            SagaEvent billed = new SagaEvent(null, null, sagaId, SagaEventType.BILLING_CHARGED, charge);
            billingChargedQueue.offer(billed);

            SagaEvent completed = new SagaEvent(null, null, sagaId, SagaEventType.SAGA_COMPLETED, null);
            sagaCompletedQueue.offer(completed);
        }
    }
}
