package com.careergps.profiles.domain.repository;

import java.util.Optional;

import com.careergps.profiles.domain.model.Estudiante;

public interface EstudianteRepository {

    Optional<Estudiante> findById(Long id);

    boolean existsById(Long id);
    Optional<Estudiante> findByCorreo(String correo);

    Estudiante save(Estudiante estudiante);

    void deleteById(Long id);
}
