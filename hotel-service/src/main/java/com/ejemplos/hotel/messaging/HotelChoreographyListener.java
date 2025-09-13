package com.ejemplos.hotel.messaging;

import com.ejemplos.common.dto.AppointmentRequest;
import com.ejemplos.common.dto.HotelReservation;
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
public class HotelChoreographyListener {

    private static final Logger log = LoggerFactory.getLogger(HotelChoreographyListener.class);

    // Function: in appointments.requested -> out hotel.reserved
    @Bean
    public Function<SagaEvent, SagaEvent> hotelProcessor() {
        return event -> {
            if (event == null || event.type() != SagaEventType.APPOINTMENT_REQUESTED)
                return null;
            log.info("Recibida solicitud de cita sagaId={} -> reservando hotel", event.sagaId());
            AppointmentRequest req = toAppointmentRequest(event.payload());
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
