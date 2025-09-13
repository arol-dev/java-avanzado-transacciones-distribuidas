package com.ejemplos.appointment.service;

import com.ejemplos.common.dto.AppointmentRequest;
import com.ejemplos.common.events.SagaEvent;
import com.ejemplos.common.events.SagaEventType;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class AppointmentService {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final Map<String, AppointmentRequest> inMemory = new HashMap<>();
    @Value("${topics.appointment-requested:appointments.requested}")
    private String appointmentRequestedTopic;

    @Value("${orchestrator.url:http://saga-orchestrator:8080}")
    private String orchestratorBaseUrl;

    public AppointmentService(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public String startAppointment(AppointmentRequest req, String mode) {
        String sagaId = UUID.randomUUID().toString();
        inMemory.put(sagaId, req);
        if ("orchestration".equalsIgnoreCase(mode)) {
            RestClient client = RestClient.create(orchestratorBaseUrl);
            client.post()
                    .uri("/api/saga/start")
                    .body(req)
                    .retrieve()
                    .toBodilessEntity();
        } else {
            SagaEvent event = new SagaEvent(null, null, sagaId, SagaEventType.APPOINTMENT_REQUESTED, req);
            kafkaTemplate.send(new ProducerRecord<>(appointmentRequestedTopic, sagaId, event));
        }
        return sagaId;
    }
}
