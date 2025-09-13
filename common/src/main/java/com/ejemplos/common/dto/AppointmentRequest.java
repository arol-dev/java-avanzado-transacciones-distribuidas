package com.ejemplos.common.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record AppointmentRequest(
        @NotBlank String customerId,
        @NotBlank String fromAirport,
        @NotBlank String toAirport,
        @NotNull LocalDate flightDate,
        @NotBlank String hotelCity,
        @NotNull Integer nights,
        @NotNull Integer amountCents // costo total estimado para el ejemplo
) {
}
