package com.ejemplos.common.events;

import java.time.Instant;
import java.util.UUID;

public record SagaEvent(
        String eventId,
        Instant timestamp,
        String sagaId, // id de la cita (appointment)
        SagaEventType type,
        Object payload // DTO específico por evento
) {
    public SagaEvent {
        if (eventId == null || eventId.isBlank()) {
            eventId = UUID.randomUUID().toString();
        }
        if (timestamp == null) {
            timestamp = Instant.now();
        }
    }
}
