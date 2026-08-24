package com.bistro.reservations.controller;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservationRequest {

    @NotBlank(message = "El nombre del cliente es obligatorio")
    private String customerName;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email no tiene un formato válido")
    private String customerEmail;

    @NotNull(message = "La fecha y hora de la reserva son obligatorias")
    private LocalDateTime reservationTime;

    @NotNull(message = "La cantidad de comensales es obligatoria")
    @Positive(message = "La cantidad de comensales debe ser mayor a cero")
    @Max(value = 12, message = "La cantidad de comensales no puede superar las 12 personas")
    private Integer partySize;
}
