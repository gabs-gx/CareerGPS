package com.careergps.demand.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import com.careergps.demand.domain.model.Habilidad;
import com.careergps.demand.domain.repository.HabilidadRepository;

public interface JpaHabilidadRepository extends JpaRepository<Habilidad, Long>, HabilidadRepository {
}
