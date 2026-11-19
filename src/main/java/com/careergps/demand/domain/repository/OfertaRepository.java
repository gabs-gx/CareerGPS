package com.careergps.demand.domain.repository;

import java.util.List;
import java.util.Optional;

import com.careergps.demand.domain.model.Oferta;

public interface OfertaRepository {

    Optional<Oferta> findById(Long id);

    List<Oferta> findAll();

    List<Oferta> findByPuestoId(Long puestoId);

    long countByPuestoId(Long puestoId);

    Oferta save(Oferta oferta);

    void deleteById(Long id);
}
