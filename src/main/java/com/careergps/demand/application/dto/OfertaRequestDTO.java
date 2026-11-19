package com.careergps.demand.application.dto;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record OfertaRequestDTO(
        @NotBlank(message = "El codigo es obligatorio") String codigo,
        @NotBlank(message = "El sector es obligatorio") String sector,
        @NotNull(message = "El puesto es obligatorio") Long puestoId,
        @NotBlank(message = "La descripcion es obligatoria") String descripcion,
        List<Long> habilidadIdsConfirmadas) {
}
