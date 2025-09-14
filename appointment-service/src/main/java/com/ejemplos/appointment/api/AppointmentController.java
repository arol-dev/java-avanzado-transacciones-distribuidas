package com.ejemplos.appointment.api;

import com.ejemplos.appointment.service.AppointmentService;
import com.ejemplos.common.dto.AppointmentRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    private static final Logger log = LoggerFactory.getLogger(AppointmentController.class);

    private final AppointmentService service;

    public AppointmentController(AppointmentService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<?> create(@Validated @RequestBody AppointmentRequest request,
                                    @RequestParam(name = "mode", defaultValue = "choreography") String mode) {
        log.info("[AppointmentController] Solicitud de creación de cita recibida customerId={} ruta={}->{} fecha={} modo={}",
                request.customerId(), request.fromAirport(), request.toAirport(), request.flightDate(), mode);
        String sagaId = service.startAppointment(request, mode);
        log.info("[AppointmentController] Saga iniciada sagaId={} modo={}", sagaId, mode);
        return ResponseEntity.accepted().body("Saga iniciada: " + sagaId + " (modo=" + mode + ")");
    }
}
