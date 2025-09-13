package com.ejemplos.billing.api;

import com.ejemplos.common.dto.AppointmentRequest;
import com.ejemplos.common.dto.BillingCharge;
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

    @PostMapping("/charge")
    public ResponseEntity<BillingCharge> charge(@RequestBody AppointmentRequest req) {
        BillingCharge ch = BillingCharge.builder()
                .chargeId(UUID.randomUUID().toString())
                .customerId(req.customerId())
                .amountCents(req.amountCents())
                .charged(true)
                .reason("Charge for appointment")
                .build();
        return ResponseEntity.ok(ch);
    }

    @PostMapping("/refund")
    public ResponseEntity<Void> refund(@RequestParam String chargeId) {
        return ResponseEntity.noContent().build();
    }
}
