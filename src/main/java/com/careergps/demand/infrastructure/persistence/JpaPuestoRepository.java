package com.careergps.demand.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import com.careergps.demand.domain.model.Puesto;
import com.careergps.demand.domain.repository.PuestoRepository;

public interface JpaPuestoRepository extends JpaRepository<Puesto, Long>, PuestoRepository {
}
