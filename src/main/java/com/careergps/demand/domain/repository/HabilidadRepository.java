package com.careergps.demand.domain.repository;

import java.util.List;
import java.util.Optional;

import com.careergps.demand.domain.model.Habilidad;

public interface HabilidadRepository {

    Optional<Habilidad> findById(Long id);

    List<Habilidad> findAll();

    Optional<Habilidad> findByNombreIgnoreCase(String nombre);

    Habilidad save(Habilidad habilidad);
}
