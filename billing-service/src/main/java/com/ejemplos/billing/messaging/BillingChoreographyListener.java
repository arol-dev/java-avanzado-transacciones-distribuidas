package com.ejemplos.billing.messaging;

import com.ejemplos.common.dto.BillingCharge;
import com.ejemplos.common.events.SagaEvent;
import com.ejemplos.common.events.SagaEventType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
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

    public BillingChoreographyListener(@Qualifier("billingChargedQueue") BlockingQueue<SagaEvent> billingChargedQueue,
                                       @Qualifier("sagaCompletedQueue") BlockingQueue<SagaEvent> sagaCompletedQueue) {
        this.billingChargedQueue = billingChargedQueue;
        this.sagaCompletedQueue = sagaCompletedQueue;
    }

    @Bean
    public Consumer<SagaEvent> flightReservedConsumer() {
        return event -> {
            if (event == null || event.type() != SagaEventType.FLIGHT_RESERVED)
                return;
            String reservationId = extract(event.payload(), "reservationId");
            log.info("[Billing] Evento FLIGHT_RESERVED sagaId={} eventId={} reservationId={}", event.sagaId(), event.eventId(), reservationId);
            flightOk.put(event.sagaId(), true);
            tryComplete(event.sagaId());
        };
    }

    @Bean
    public Consumer<SagaEvent> hotelReservedConsumer() {
        return event -> {
            if (event == null || event.type() != SagaEventType.HOTEL_RESERVED)
                return;
            String reservationId = extract(event.payload(), "reservationId");
            log.info("[Billing] Evento HOTEL_RESERVED sagaId={} eventId={} reservationId={}", event.sagaId(), event.eventId(), reservationId);
            hotelOk.put(event.sagaId(), true);
            tryComplete(event.sagaId());
        };
    }

    private void tryComplete(String sagaId) {
        boolean f = Boolean.TRUE.equals(flightOk.get(sagaId));
        boolean h = Boolean.TRUE.equals(hotelOk.get(sagaId));
        log.info("[Billing] Estado actual sagaId={} flightOk={} hotelOk={}", sagaId, f, h);
        if (f && h) {
            log.info("[Billing] Ambas reservas listas, procediendo a cobro sagaId={}", sagaId);
            BillingCharge charge = new BillingCharge(
                    UUID.randomUUID().toString(),
                    "unknown",
                    0,
                    true,
                    null
            );
            SagaEvent billed = new SagaEvent(null, null, sagaId, SagaEventType.BILLING_CHARGED, charge);
            boolean offered1 = billingChargedQueue.offer(billed);
            log.info("[Billing] Encolado BILLING_CHARGED sagaId={} chargeId={} eventId={} offered={}", sagaId, charge.chargeId(), billed.eventId(), offered1);

            SagaEvent completed = new SagaEvent(null, null, sagaId, SagaEventType.SAGA_COMPLETED, null);
            boolean offered2 = sagaCompletedQueue.offer(completed);
            log.info("[Billing] Encolado SAGA_COMPLETED sagaId={} eventId={} offered={}", sagaId, completed.eventId(), offered2);
        }
    }

    private String extract(Object payload, String key) {
        if (payload instanceof Map<?, ?> m) {
            Object val = m.get(key);
            return val != null ? String.valueOf(val) : null;
        }
        try {
            return (String) payload.getClass().getMethod(key).invoke(payload);
        } catch (Exception ignored) {
            return null;
        }
    }
}
