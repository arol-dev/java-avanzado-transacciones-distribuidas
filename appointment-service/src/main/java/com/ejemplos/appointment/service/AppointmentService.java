package com.ejemplos.appointment.service;

import com.ejemplos.common.dto.AppointmentRequest;
import com.ejemplos.common.events.SagaEvent;
import com.ejemplos.common.events.SagaEventType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;

@Service
public class AppointmentService {

    private final BlockingQueue<SagaEvent> outbox;
    private final Map<String, AppointmentRequest> inMemory = new HashMap<>();

    @Value("${orchestrator.url:http://saga-orchestrator:8080}")
    private String orchestratorBaseUrl;

    public AppointmentService(BlockingQueue<SagaEvent> outbox) {
        this.outbox = outbox;
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
            // Enqueue for Supplier to publish via Spring Cloud Stream
            outbox.offer(event);
        }
        return sagaId;
    }
}
