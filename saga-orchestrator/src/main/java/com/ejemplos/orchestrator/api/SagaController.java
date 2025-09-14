package com.ejemplos.orchestrator.api;

import com.ejemplos.common.dto.AppointmentRequest;
import com.ejemplos.common.dto.BillingCharge;
import com.ejemplos.common.dto.FlightReservation;
import com.ejemplos.common.dto.HotelReservation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
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

    private final RestClient flightClient;
    private final RestClient hotelClient;
    private final RestClient billingClient;

    public SagaController(RestClient.Builder builder,
                          @Value("${services.flight}") String flightBaseUrl,
                          @Value("${services.hotel}") String hotelBaseUrl,
                          @Value("${services.billing}") String billingBaseUrl) {
        this.flightClient = builder.baseUrl(flightBaseUrl).build();
        this.hotelClient = builder.baseUrl(hotelBaseUrl).build();
        this.billingClient = builder.baseUrl(billingBaseUrl).build();
    }

    @PostMapping("/start")
    public ResponseEntity<String> start(@RequestBody AppointmentRequest req) {
        log.info("[Orchestrator] Inicio de SAGA (orquestación) customerId={} ruta={}->{} fecha={} monto={}cents",
                req.customerId(), req.fromAirport(), req.toAirport(), req.flightDate(), req.amountCents());
        FlightReservation fr = null;
        HotelReservation hr = null;
        BillingCharge ch = null;
        try {
            fr = flightClient.post().uri("/api/flight/reserve").body(req).retrieve().body(FlightReservation.class);
            log.info("[Orchestrator] Vuelo reservado reservationId={} from={} to={} date={}", Objects.requireNonNull(fr).reservationId(), fr.fromAirport(), fr.toAirport(), fr.date());
            hr = hotelClient.post().uri("/api/hotel/reserve").body(req).retrieve().body(HotelReservation.class);
            log.info("[Orchestrator] Hotel reservado reservationId={} city={} nights={}", Objects.requireNonNull(hr).reservationId(), hr.city(), hr.nights());
            ch = billingClient.post().uri("/api/billing/charge").body(req).retrieve().body(BillingCharge.class);
            log.info("[Orchestrator] Cobro aplicado chargeId={} customerId={} amount={}cents", Objects.requireNonNull(ch).chargeId(), ch.customerId(), ch.amountCents());
            return ResponseEntity.accepted().body("SAGA completada (orquestación): vuelo=" + fr.reservationId() + ", hotel=" + hr.reservationId() + ", cargo=" + ch.chargeId());
        } catch (Exception e) {
            log.error("[Orchestrator] Fallo en la orquestación, aplicando compensaciones. causa={}", e.getMessage(), e);
            // Compensaciones básicas best-effort
            try {
                if (ch != null && ch.charged()) {
                    log.info("[Orchestrator] Compensación: refund chargeId={}", ch.chargeId());
                    billingClient.post().uri("/api/billing/refund?chargeId=" + ch.chargeId()).retrieve().toBodilessEntity();
                }
            } catch (Exception ignored) {
                log.warn("[Orchestrator] Fallo al compensar refund chargeId={}", ch.chargeId());
            }
            try {
                if (hr != null && hr.confirmed()) {
                    log.info("[Orchestrator] Compensación: cancel hotel reservationId={}", hr.reservationId());
                    hotelClient.post().uri("/api/hotel/cancel?reservationId=" + hr.reservationId()).retrieve().toBodilessEntity();
                }
            } catch (Exception ignored) {
                log.warn("[Orchestrator] Fallo al compensar cancel hotel reservationId={}", hr.reservationId());
            }
            try {
                if (fr != null && fr.confirmed()) {
                    log.info("[Orchestrator] Compensación: cancel flight reservationId={}", fr.reservationId());
                    flightClient.post().uri("/api/flight/cancel?reservationId=" + fr.reservationId()).retrieve().toBodilessEntity();
                }
            } catch (Exception ignored) {
                log.warn("[Orchestrator] Fallo al compensar cancel flight reservationId={}", fr.reservationId());
            }
            return ResponseEntity.internalServerError().body("SAGA fallida y compensada parcialmente");
        }
    }
}
