package com.ejemplos.billing.api;

import com.ejemplos.common.dto.AppointmentRequest;
import com.ejemplos.common.dto.BillingCharge;
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
@RequestMapping("/api/billing")
public class BillingController {

    private static final Logger log = LoggerFactory.getLogger(BillingController.class);

    @PostMapping("/charge")
    public ResponseEntity<BillingCharge> charge(@RequestBody AppointmentRequest req) {
        BillingCharge ch = BillingCharge.builder()
                .chargeId(UUID.randomUUID().toString())
                .customerId(req.customerId())
                .amountCents(req.amountCents())
                .charged(true)
                .reason("Charge for appointment")
                .build();
        log.info("[BillingController] Cargo aplicado chargeId={} customerId={} amount={}cents", ch.chargeId(), ch.customerId(), ch.amountCents());
        return ResponseEntity.ok(ch);
    }

    @PostMapping("/refund")
    public ResponseEntity<Void> refund(@RequestParam String chargeId) {
        log.info("[BillingController] Reembolso solicitado chargeId={}", chargeId);
        return ResponseEntity.noContent().build();
    }
}
