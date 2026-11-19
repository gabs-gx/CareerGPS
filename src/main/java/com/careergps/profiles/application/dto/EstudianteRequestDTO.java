package com.careergps.profiles.application.dto;

import jakarta.validation.constraints.NotBlank;

public record EstudianteRequestDTO(@NotBlank(message = "La carrera es obligatoria") String carrera) {
}
