package com.careergps.profiles.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record NuevaAlertaDTO(
        @NotBlank(message = "El tipo de alerta es obligatorio")
        @Pattern(regexp = "sube|baja|mercado|meta|info", message = "El tipo de alerta no es valido") String tipo,
        @NotBlank(message = "El texto de la alerta es obligatorio")
        @Size(max = 300, message = "El texto de la alerta es demasiado largo") String texto) {
}
