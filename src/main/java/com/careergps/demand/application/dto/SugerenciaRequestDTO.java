package com.careergps.demand.application.dto;

import jakarta.validation.constraints.NotBlank;

public record SugerenciaRequestDTO(@NotBlank(message = "La descripcion es obligatoria") String descripcion) {
}
