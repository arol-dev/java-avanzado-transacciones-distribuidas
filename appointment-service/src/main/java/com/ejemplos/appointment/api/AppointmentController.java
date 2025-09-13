package com.ejemplos.appointment.api;

import com.ejemplos.appointment.service.AppointmentService;
import com.ejemplos.common.dto.AppointmentRequest;
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

    private final AppointmentService service;

    public AppointmentController(AppointmentService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<?> create(@Validated @RequestBody AppointmentRequest request,
                                    @RequestParam(defaultValue = "choreography") String mode) {
        String sagaId = service.startAppointment(request, mode);
        return ResponseEntity.accepted().body("Saga iniciada: " + sagaId + " (modo=" + mode + ")");
    }
}
