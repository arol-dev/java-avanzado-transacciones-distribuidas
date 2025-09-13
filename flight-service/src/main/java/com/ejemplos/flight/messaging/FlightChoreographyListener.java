package com.ejemplos.flight.messaging;

import com.ejemplos.common.dto.AppointmentRequest;
import com.ejemplos.common.dto.FlightReservation;
import com.ejemplos.common.events.SagaEvent;
import com.ejemplos.common.events.SagaEventType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
            AppointmentRequest req = (AppointmentRequest) event.payload();
            FlightReservation fr = new FlightReservation(
                    UUID.randomUUID().toString(),
                    req.fromAirport(),
                    req.toAirport(),
                    req.flightDate().toString(),
                    true,
                    null
            );
            return new SagaEvent(null, null, event.sagaId(), SagaEventType.FLIGHT_RESERVED, fr);
        };
    }
}
