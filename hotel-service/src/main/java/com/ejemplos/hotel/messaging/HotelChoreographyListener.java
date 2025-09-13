package com.ejemplos.hotel.messaging;

import com.ejemplos.common.dto.AppointmentRequest;
import com.ejemplos.common.dto.HotelReservation;
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
public class HotelChoreographyListener {

    private static final Logger log = LoggerFactory.getLogger(HotelChoreographyListener.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;
    @Value("${topics.appointment-requested:appointments.requested}")
    private String appointmentRequestedTopic;
    @Value("${topics.hotel-reserved:hotel.reserved}")
    private String hotelReservedTopic;

    public HotelChoreographyListener(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @KafkaListener(topics = "#{'${topics.appointment-requested:appointments.requested}'}", groupId = "hotel-service")
    public void onAppointmentRequested(@Payload SagaEvent event) {
        if (event.type() != SagaEventType.APPOINTMENT_REQUESTED) return;
        log.info("Recibida solicitud de cita sagaId={} -> reservando hotel", event.sagaId());
        AppointmentRequest req = (AppointmentRequest) event.payload();
        HotelReservation hr = new HotelReservation(
                UUID.randomUUID().toString(),
                req.hotelCity(),
                req.nights(),
                true,
                null
        );
        SagaEvent out = new SagaEvent(null, null, event.sagaId(), SagaEventType.HOTEL_RESERVED, hr);
        kafkaTemplate.send(new ProducerRecord<>(hotelReservedTopic, event.sagaId(), out));
    }
}
