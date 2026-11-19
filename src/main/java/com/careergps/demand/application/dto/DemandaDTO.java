package com.careergps.demand.application.dto;

import java.util.List;

public record DemandaDTO(
        Long puestoId,
        String puestoNombre,
        int totalOfertas,
        List<HabilidadFrecuencia> habilidades) {

    public record HabilidadFrecuencia(Long habilidadId, String nombre, long frecuencia, double porcentaje) {
    }
}
