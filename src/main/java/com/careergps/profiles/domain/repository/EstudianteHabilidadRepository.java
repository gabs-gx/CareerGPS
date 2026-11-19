package com.careergps.profiles.domain.repository;

import java.util.List;

import com.careergps.profiles.domain.model.EstudianteHabilidad;

public interface EstudianteHabilidadRepository {

    List<EstudianteHabilidad> findByEstudianteId(Long estudianteId);

    EstudianteHabilidad save(EstudianteHabilidad estudianteHabilidad);

    void deleteByEstudianteId(Long estudianteId);
}
