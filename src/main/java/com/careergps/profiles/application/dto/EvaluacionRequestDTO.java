package com.careergps.profiles.application.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;

public record EvaluacionRequestDTO(
        @Size(max = 4000, message = "El resumen de alineacion es demasiado largo") String snapshot,
        @Valid @Size(max = 20, message = "Maximo 20 alertas por evaluacion") List<NuevaAlertaDTO> nuevas) {
}
