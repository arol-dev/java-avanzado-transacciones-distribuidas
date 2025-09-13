package com.ejemplos.flight.messaging;

import com.ejemplos.common.dto.AppointmentRequest;
import com.ejemplos.common.dto.FlightReservation;
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

import java.util.UUID;

@Component
public class FlightChoreographyListener {

    private static final Logger log = LoggerFactory.getLogger(FlightChoreographyListener.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;
    @Value("${topics.appointment-requested:appointments.requested}")
    private String appointmentRequestedTopic;
    @Value("${topics.flight-reserved:flight.reserved}")
    private String flightReservedTopic;

    public FlightChoreographyListener(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @KafkaListener(topics = "#{'${topics.appointment-requested:appointments.requested}'}", groupId = "flight-service")
    public void onAppointmentRequested(@Payload SagaEvent event) {
        if (event.type() != SagaEventType.APPOINTMENT_REQUESTED) return;
        log.info("Recibida solicitud de cita sagaId={} -> reservando vuelo", event.sagaId());
        AppointmentRequest req = (AppointmentRequest) event.payload();
        FlightReservation fr = new FlightReservation(
                UUID.randomUUID().toString(),
                req.fromAirport(),
                req.toAirport(),
                req.flightDate().toString(),
                true,
                null
        );
        SagaEvent out = new SagaEvent(null, null, event.sagaId(), SagaEventType.FLIGHT_RESERVED, fr);
        kafkaTemplate.send(new ProducerRecord<>(flightReservedTopic, event.sagaId(), out));
    }
}
