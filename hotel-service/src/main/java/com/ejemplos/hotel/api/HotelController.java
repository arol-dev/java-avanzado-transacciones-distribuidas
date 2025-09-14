package com.ejemplos.hotel.api;

import com.ejemplos.common.dto.AppointmentRequest;
import com.ejemplos.common.dto.HotelReservation;
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
@RequestMapping("/api/hotel")
public class HotelController {

    private static final Logger log = LoggerFactory.getLogger(HotelController.class);

    @PostMapping("/reserve")
    public ResponseEntity<HotelReservation> reserve(@RequestBody AppointmentRequest req) {
        HotelReservation hr = HotelReservation.builder()
                .reservationId(UUID.randomUUID().toString())
                .city(req.hotelCity())
                .nights(req.nights())
                .confirmed(true)
                .build();
        log.info("[HotelController] Reserva de hotel confirmada reservationId={} city={} nights={} customerId={}",
                hr.reservationId(), hr.city(), hr.nights(), req.customerId());
        return ResponseEntity.ok(hr);
    }

    @PostMapping("/cancel")
    public ResponseEntity<Void> cancel(@RequestParam String reservationId) {
        log.info("[HotelController] Cancelación recibida reservationId={}", reservationId);
        return ResponseEntity.noContent().build();
    }
}
