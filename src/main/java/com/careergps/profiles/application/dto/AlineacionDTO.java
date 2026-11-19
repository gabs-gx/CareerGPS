package com.careergps.profiles.application.dto;

import java.util.List;

public record AlineacionDTO(
        Long puestoId,
        String puestoNombre,
        List<String> coincidencias,
        List<String> aFortalecer) {
}
