package com.ejemplos.orchestrator.api;

import com.ejemplos.common.dto.AppointmentRequest;
import com.ejemplos.common.dto.BillingCharge;
import com.ejemplos.common.dto.FlightReservation;
import com.ejemplos.common.dto.HotelReservation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

import java.util.Objects;

@RestController
@RequestMapping("/api/saga")
public class SagaController {

    private static final Logger log = LoggerFactory.getLogger(SagaController.class);

    private final RestClient flightClient = RestClient.create("http://flight-service:8082");
    private final RestClient hotelClient = RestClient.create("http://hotel-service:8083");
    private final RestClient billingClient = RestClient.create("http://billing-service:8084");

    @PostMapping("/start")
    public ResponseEntity<String> start(@RequestBody AppointmentRequest req) {
        FlightReservation fr = null;
        HotelReservation hr = null;
        BillingCharge ch = null;
        try {
            fr = flightClient.post().uri("/api/flight/reserve").body(req).retrieve().body(FlightReservation.class);
            hr = hotelClient.post().uri("/api/hotel/reserve").body(req).retrieve().body(HotelReservation.class);
            ch = billingClient.post().uri("/api/billing/charge").body(req).retrieve().body(BillingCharge.class);
            return ResponseEntity.accepted().body("SAGA completada (orquestación): vuelo=" + Objects.requireNonNull(fr).reservationId() + ", hotel=" + Objects.requireNonNull(hr).reservationId() + ", cargo=" + Objects.requireNonNull(ch).chargeId());
        } catch (Exception e) {
            log.error("Fallo en la orquestación, aplicando compensaciones: {}", e.getMessage());
            // Compensaciones básicas best-effort
            try {
                if (ch != null && ch.charged())
                    billingClient.post().uri("/api/billing/refund?chargeId=" + ch.chargeId()).retrieve().toBodilessEntity();
            } catch (Exception ignored) {
                //ignored
            }
            try {
                if (hr != null && hr.confirmed())
                    hotelClient.post().uri("/api/hotel/cancel?reservationId=" + hr.reservationId()).retrieve().toBodilessEntity();
            } catch (Exception ignored) {
                //ignored
            }
            try {
                if (fr != null && fr.confirmed())
                    flightClient.post().uri("/api/flight/cancel?reservationId=" + fr.reservationId()).retrieve().toBodilessEntity();
            } catch (Exception ignored) {
                //ignored
            }
            return ResponseEntity.internalServerError().body("SAGA fallida y compensada parcialmente");
        }
    }
}
