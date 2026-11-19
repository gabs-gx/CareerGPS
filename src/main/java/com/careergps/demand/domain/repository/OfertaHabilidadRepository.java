package com.careergps.demand.domain.repository;

import java.util.List;

import com.careergps.demand.domain.model.FrecuenciaHabilidad;
import com.careergps.demand.domain.model.OfertaHabilidad;

public interface OfertaHabilidadRepository {

    List<FrecuenciaHabilidad> contarFrecuenciaPorPuesto(Long puestoId);

    OfertaHabilidad save(OfertaHabilidad ofertaHabilidad);

    void deleteByOfertaId(Long ofertaId);
}
