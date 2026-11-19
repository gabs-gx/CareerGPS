package com.careergps.profiles.application.service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.careergps.demand.application.dto.DemandaDTO;
import com.careergps.demand.application.dto.DemandaDTO.HabilidadFrecuencia;
import com.careergps.demand.application.service.PuestoService;
import com.careergps.profiles.application.dto.AlineacionDTO;
import com.careergps.profiles.domain.model.EstudianteHabilidad;
import com.careergps.profiles.domain.repository.EstudianteHabilidadRepository;
import com.careergps.profiles.domain.repository.EstudianteRepository;
import com.careergps.shared.domain.exception.RecursoNoEncontradoException;

@Service
public class AlineacionService {

    private final EstudianteRepository estudianteRepository;
    private final EstudianteHabilidadRepository estudianteHabilidadRepository;
    private final PuestoService puestoService;

    public AlineacionService(
            EstudianteRepository estudianteRepository,
            EstudianteHabilidadRepository estudianteHabilidadRepository,
            PuestoService puestoService) {
        this.estudianteRepository = estudianteRepository;
        this.estudianteHabilidadRepository = estudianteHabilidadRepository;
        this.puestoService = puestoService;
    }

    public AlineacionDTO calcularAlineacion(Long estudianteId, Long puestoId) {
        if (!estudianteRepository.existsById(estudianteId)) {
            throw new RecursoNoEncontradoException("Estudiante no encontrado: " + estudianteId);
        }

        DemandaDTO demanda = puestoService.calcularDemanda(puestoId);

        Set<Long> habilidadIdsDelEstudiante = estudianteHabilidadRepository.findByEstudianteId(estudianteId).stream()
                .filter(eh -> !"no tiene".equalsIgnoreCase(eh.getNivel()))
                .map(EstudianteHabilidad::getHabilidadId)
                .collect(Collectors.toSet());

        List<String> coincidencias = demanda.habilidades().stream()
                .filter(h -> habilidadIdsDelEstudiante.contains(h.habilidadId()))
                .map(HabilidadFrecuencia::nombre)
                .toList();

        List<String> aFortalecer = demanda.habilidades().stream()
                .filter(h -> !habilidadIdsDelEstudiante.contains(h.habilidadId()))
                .map(HabilidadFrecuencia::nombre)
                .toList();

        return new AlineacionDTO(puestoId, demanda.puestoNombre(), coincidencias, aFortalecer);
    }
}
