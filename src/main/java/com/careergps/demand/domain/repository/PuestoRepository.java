package com.careergps.demand.domain.repository;

import java.util.List;
import java.util.Optional;

import com.careergps.demand.domain.model.Puesto;

public interface PuestoRepository {

    Optional<Puesto> findById(Long id);

    List<Puesto> findAll();

    Puesto save(Puesto puesto);
}
