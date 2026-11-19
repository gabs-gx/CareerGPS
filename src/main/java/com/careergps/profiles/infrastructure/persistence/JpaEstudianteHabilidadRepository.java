package com.careergps.profiles.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import com.careergps.profiles.domain.model.EstudianteHabilidad;
import com.careergps.profiles.domain.repository.EstudianteHabilidadRepository;

public interface JpaEstudianteHabilidadRepository extends JpaRepository<EstudianteHabilidad, Long>, EstudianteHabilidadRepository {
}
