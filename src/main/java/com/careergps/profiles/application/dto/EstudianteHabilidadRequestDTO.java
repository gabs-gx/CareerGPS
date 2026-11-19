package com.careergps.profiles.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record EstudianteHabilidadRequestDTO(
        @NotNull(message = "La habilidad es obligatoria") Long habilidadId,
        @NotBlank(message = "El nivel es obligatorio")
        @Pattern(regexp = "(?i)no tiene|basico|dominado", message = "El nivel debe ser: no tiene, basico o dominado")
        String nivel) {
}
