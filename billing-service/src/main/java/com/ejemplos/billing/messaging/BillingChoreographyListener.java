package com.ejemplos.billing.messaging;

import com.ejemplos.common.dto.BillingCharge;
import com.ejemplos.common.events.SagaEvent;
import com.ejemplos.common.events.SagaEventType;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class BillingChoreographyListener {

    private static final Logger log = LoggerFactory.getLogger(BillingChoreographyListener.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final Map<String, Boolean> flightOk = new HashMap<>();
    private final Map<String, Boolean> hotelOk = new HashMap<>();
    @Value("${topics.flight-reserved:flight.reserved}")
    private String flightReservedTopic;
    @Value("${topics.hotel-reserved:hotel.reserved}")
    private String hotelReservedTopic;
    @Value("${topics.billing-charged:billing.charged}")
    private String billingChargedTopic;
    @Value("${topics.saga-completed:saga.completed}")
    private String sagaCompletedTopic;
    public BillingChoreographyListener(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @KafkaListener(topics = "#{'${topics.flight-reserved:flight.reserved}'}", groupId = "billing-service")
    public void onFlightReserved(@Payload SagaEvent event) {
        if (event.type() != SagaEventType.FLIGHT_RESERVED) return;
        log.info("Vuelo reservado para sagaId={}", event.sagaId());
        flightOk.put(event.sagaId(), true);
        tryComplete(event.sagaId());
    }

    @KafkaListener(topics = "#'{${topics.hotel-reserved:hotel.reserved}}'", groupId = "billing-service")
    public void onHotelReserved(@Payload SagaEvent event) {
        if (event.type() != SagaEventType.HOTEL_RESERVED) return;
        log.info("Hotel reservado para sagaId={}", event.sagaId());
        hotelOk.put(event.sagaId(), true);
        tryComplete(event.sagaId());
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
            kafkaTemplate.send(new ProducerRecord<>(billingChargedTopic, sagaId, billed));

            SagaEvent completed = new SagaEvent(null, null, sagaId, SagaEventType.SAGA_COMPLETED, null);
            kafkaTemplate.send(new ProducerRecord<>(sagaCompletedTopic, sagaId, completed));
        }
    }
}
