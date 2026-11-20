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
import com.careergps.profiles.domain.repository.AlertaRepository;
import com.careergps.profiles.domain.repository.EstudianteHabilidadRepository;
import com.careergps.profiles.domain.repository.EstudianteRepository;
import com.careergps.shared.domain.exception.RecursoNoEncontradoException;

@Service
public class EstudianteService {

    private final EstudianteRepository estudianteRepository;
    private final HabilidadRepository habilidadRepository;
    private final EstudianteHabilidadRepository estudianteHabilidadRepository;
    private final AlertaRepository alertaRepository;

    public EstudianteService(
            EstudianteRepository estudianteRepository,
            HabilidadRepository habilidadRepository,
            EstudianteHabilidadRepository estudianteHabilidadRepository,
            AlertaRepository alertaRepository) {
        this.estudianteRepository = estudianteRepository;
        this.habilidadRepository = habilidadRepository;
        this.estudianteHabilidadRepository = estudianteHabilidadRepository;
        this.alertaRepository = alertaRepository;
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
        alertaRepository.deleteByEstudianteId(id);
        estudianteRepository.deleteById(id);
    }

    public EstudianteHabilidad registrarHabilidad(Long estudianteId, EstudianteHabilidadRequestDTO dto) {
        Estudiante estudiante = obtenerPerfil(estudianteId);
        Habilidad habilidad = habilidadRepository.findById(dto.habilidadId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Habilidad no encontrada: " + dto.habilidadId()));

        EstudianteHabilidad registro = estudianteHabilidadRepository.findByEstudianteId(estudiante.getId()).stream()
                .filter(h -> h.getHabilidadId().equals(habilidad.getId()))
                .findFirst()
                .orElseGet(() -> new EstudianteHabilidad(estudiante.getId(), habilidad.getId(), dto.nivel()));
        registro.setNivel(dto.nivel());
        return estudianteHabilidadRepository.save(registro);
    }

    public List<EstudianteHabilidad> listarHabilidades(Long estudianteId) {
        return estudianteHabilidadRepository.findByEstudianteId(estudianteId);
    }
}
