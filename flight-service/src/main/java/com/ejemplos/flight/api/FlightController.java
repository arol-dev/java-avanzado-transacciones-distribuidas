package com.ejemplos.flight.api;

import com.ejemplos.common.dto.AppointmentRequest;
import com.ejemplos.common.dto.FlightReservation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/flight")
public class FlightController {

    private static final Logger log = LoggerFactory.getLogger(FlightController.class);

    @PostMapping("/reserve")
    public ResponseEntity<FlightReservation> reserve(@RequestBody AppointmentRequest req) {
        FlightReservation fr = FlightReservation.builder()
                .reservationId(UUID.randomUUID().toString())
                .fromAirport(req.fromAirport())
                .toAirport(req.toAirport())
                .date(req.flightDate().toString())
                .confirmed(true)
                .build();
        log.info("[FlightController] Reserva confirmada reservationId={} {}->{} date={} customerId={}",
                fr.reservationId(), fr.fromAirport(), fr.toAirport(), fr.date(), req.customerId());
        return ResponseEntity.ok(fr);
    }

    @PostMapping("/cancel")
    public ResponseEntity<Void> cancel(@RequestParam String reservationId) {
        log.info("[FlightController] Cancelación recibida reservationId={}", reservationId);
        return ResponseEntity.noContent().build();
    }
}
