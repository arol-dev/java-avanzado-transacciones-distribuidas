package com.ejemplos.flight.messaging;

import com.ejemplos.common.dto.AppointmentRequest;
import com.ejemplos.common.dto.FlightReservation;
import com.ejemplos.common.events.SagaEvent;
import com.ejemplos.common.events.SagaEventType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

@Configuration
public class FlightChoreographyListener {

    private static final Logger log = LoggerFactory.getLogger(FlightChoreographyListener.class);

    // Function: in appointments.requested -> out flight.reserved
    @Bean
    public Function<SagaEvent, SagaEvent> flightProcessor() {
        return event -> {
            if (event == null || event.type() != SagaEventType.APPOINTMENT_REQUESTED)
                return null;
            log.info("Recibida solicitud de cita sagaId={} -> reservando vuelo", event.sagaId());
            AppointmentRequest req = toAppointmentRequest(event.payload());
            FlightReservation fr = new FlightReservation(
                    UUID.randomUUID().toString(),
                    req.fromAirport(),
                    req.toAirport(),
                    req.flightDate() != null ? req.flightDate().toString() : null,
                    true,
                    null
            );
            return new SagaEvent(null, null, event.sagaId(), SagaEventType.FLIGHT_RESERVED, fr);
        };
    }

    private AppointmentRequest toAppointmentRequest(Object payload) {
        if (payload instanceof AppointmentRequest ar) {
            return ar;
        }
        if (payload instanceof Map<?, ?> map) {
            String customerId = (String) map.get("customerId");
            String fromAirport = (String) map.get("fromAirport");
            String toAirport = (String) map.get("toAirport");
            String flightDateStr = (String) map.get("flightDate");
            String hotelCity = (String) map.get("hotelCity");
            Integer nights = (map.get("nights") instanceof Number n) ? n.intValue() : null;
            Integer amountCents = (map.get("amountCents") instanceof Number n2) ? n2.intValue() : null;
            return new AppointmentRequest(
                    customerId,
                    fromAirport,
                    toAirport,
                    flightDateStr != null ? LocalDate.parse(flightDateStr) : null,
                    hotelCity,
                    nights,
                    amountCents
            );
        }
        throw new IllegalArgumentException("Unsupported payload type: " + (payload == null ? "null" : payload.getClass()));
    }
}
