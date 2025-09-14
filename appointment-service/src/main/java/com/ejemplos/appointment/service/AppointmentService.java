package com.ejemplos.appointment.service;

import com.ejemplos.common.dto.AppointmentRequest;
import com.ejemplos.common.events.SagaEvent;
import com.ejemplos.common.events.SagaEventType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;

@Service
public class AppointmentService {

    private static final Logger log = LoggerFactory.getLogger(AppointmentService.class);

    private final BlockingQueue<SagaEvent> outbox;
    private final Map<String, AppointmentRequest> inMemory = new HashMap<>();

    @Value("${orchestrator.url}")
    private String orchestratorBaseUrl;

    public AppointmentService(BlockingQueue<SagaEvent> outbox) {
        this.outbox = outbox;
    }

    public String startAppointment(AppointmentRequest req, String mode) {
        String sagaId = UUID.randomUUID().toString();
        inMemory.put(sagaId, req);
        log.info("[AppointmentService] Iniciando SAGA sagaId={} modo={} customerId={} ruta={}->{} fecha={} monto={}cents",
                sagaId, mode, req.customerId(), req.fromAirport(), req.toAirport(), req.flightDate(), req.amountCents());
        if ("orchestration".equalsIgnoreCase(mode)) {
            log.info("[AppointmentService] Llamando a orquestador {} /api/saga/start para sagaId={}", orchestratorBaseUrl, sagaId);
            RestClient client = RestClient.create(orchestratorBaseUrl);
            client.post()
                    .uri("/api/saga/start")
                    .body(req)
                    .retrieve()
                    .toBodilessEntity();
            log.info("[AppointmentService] Invocación a orquestador enviada sagaId={}", sagaId);
        } else {
            SagaEvent event = new SagaEvent(null, null, sagaId, SagaEventType.APPOINTMENT_REQUESTED, req);
            boolean offered = outbox.offer(event);
            log.info("[AppointmentService] Evento encolado para publicar sagaId={} type={} offered={} eventId={} ts={}",
                    sagaId, event.type(), offered, event.eventId(), event.timestamp());
        }
        return sagaId;
    }
}
