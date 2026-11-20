package com.careergps.profiles.domain.repository;

import java.util.List;

import com.careergps.profiles.domain.model.Alerta;

public interface AlertaRepository {
    List<Alerta> findByEstudianteIdOrderByCreadaEnDescIdDesc(Long estudianteId);
    Alerta save(Alerta alerta);
    void deleteById(Long id);
    void deleteByEstudianteId(Long estudianteId);
}
