package com.careergps.profiles.application.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.careergps.demand.domain.model.Habilidad;
import com.careergps.demand.domain.repository.HabilidadRepository;
import com.careergps.profiles.application.dto.EstudianteHabilidadRequestDTO;
import com.careergps.profiles.application.dto.EstudianteRequestDTO;
import com.careergps.profiles.domain.model.Estudiante;
import com.careergps.profiles.domain.model.EstudianteHabilidad;
import com.careergps.profiles.domain.repository.EstudianteHabilidadRepository;
import com.careergps.profiles.domain.repository.EstudianteRepository;
import com.careergps.shared.domain.exception.RecursoNoEncontradoException;

@Service
public class EstudianteService {

    private final EstudianteRepository estudianteRepository;
    private final HabilidadRepository habilidadRepository;
    private final EstudianteHabilidadRepository estudianteHabilidadRepository;

    public EstudianteService(
            EstudianteRepository estudianteRepository,
            HabilidadRepository habilidadRepository,
            EstudianteHabilidadRepository estudianteHabilidadRepository) {
        this.estudianteRepository = estudianteRepository;
        this.habilidadRepository = habilidadRepository;
        this.estudianteHabilidadRepository = estudianteHabilidadRepository;
    }

    public Estudiante registrarPerfil(EstudianteRequestDTO dto) {
        return estudianteRepository.save(new Estudiante(dto.carrera()));
    }

    public Estudiante obtenerPerfil(Long id) {
        return estudianteRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Estudiante no encontrado: " + id));
    }

    public Estudiante actualizarPerfil(Long id, EstudianteRequestDTO dto) {
        Estudiante estudiante = obtenerPerfil(id);
        estudiante.setCarrera(dto.carrera());
        return estudianteRepository.save(estudiante);
    }

    @Transactional
    public void eliminarPerfil(Long id) {
        obtenerPerfil(id);
        estudianteHabilidadRepository.deleteByEstudianteId(id);
        estudianteRepository.deleteById(id);
    }

    public EstudianteHabilidad registrarHabilidad(Long estudianteId, EstudianteHabilidadRequestDTO dto) {
        Estudiante estudiante = obtenerPerfil(estudianteId);
        Habilidad habilidad = habilidadRepository.findById(dto.habilidadId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Habilidad no encontrada: " + dto.habilidadId()));

        return estudianteHabilidadRepository.save(
                new EstudianteHabilidad(estudiante.getId(), habilidad.getId(), dto.nivel()));
    }

    public List<EstudianteHabilidad> listarHabilidades(Long estudianteId) {
        return estudianteHabilidadRepository.findByEstudianteId(estudianteId);
    }
}
