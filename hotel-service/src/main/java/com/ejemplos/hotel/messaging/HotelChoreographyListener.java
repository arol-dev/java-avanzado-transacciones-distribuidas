package com.ejemplos.hotel.messaging;

import com.ejemplos.common.dto.AppointmentRequest;
import com.ejemplos.common.dto.HotelReservation;
import com.ejemplos.common.events.SagaEvent;
import com.ejemplos.common.events.SagaEventType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.UUID;
import java.util.function.Function;

@Configuration
public class HotelChoreographyListener {

    private static final Logger log = LoggerFactory.getLogger(HotelChoreographyListener.class);

    // Function: in appointments.requested -> out hotel.reserved
    @Bean
    public Function<SagaEvent, SagaEvent> hotelProcessor() {
        return event -> {
            if (event == null || event.type() != SagaEventType.APPOINTMENT_REQUESTED)
                return null;
            log.info("Recibida solicitud de cita sagaId={} -> reservando hotel", event.sagaId());
            AppointmentRequest req = (AppointmentRequest) event.payload();
            HotelReservation hr = new HotelReservation(
                    UUID.randomUUID().toString(),
                    req.hotelCity(),
                    req.nights(),
                    true,
                    null
            );
            return new SagaEvent(null, null, event.sagaId(), SagaEventType.HOTEL_RESERVED, hr);
        };
    }
}
