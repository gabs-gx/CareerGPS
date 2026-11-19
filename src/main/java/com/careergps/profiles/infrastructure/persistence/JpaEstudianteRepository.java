package com.careergps.profiles.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import com.careergps.profiles.domain.model.Estudiante;
import com.careergps.profiles.domain.repository.EstudianteRepository;

public interface JpaEstudianteRepository extends JpaRepository<Estudiante, Long>, EstudianteRepository {
}
