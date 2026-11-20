package com.careergps.profiles.application.service;

import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.careergps.profiles.application.dto.AlertaDTO;
import com.careergps.profiles.application.dto.AlertasDTO;
import com.careergps.profiles.application.dto.EvaluacionRequestDTO;
import com.careergps.profiles.application.dto.NuevaAlertaDTO;
import com.careergps.profiles.domain.model.Alerta;
import com.careergps.profiles.domain.model.Estudiante;
import com.careergps.profiles.domain.repository.AlertaRepository;
import com.careergps.profiles.domain.repository.EstudianteRepository;
import com.careergps.shared.domain.exception.RecursoNoEncontradoException;

@Service
public class AlertaService {

    private static final int MAXIMO_ALERTAS = 30;

    private final AlertaRepository alertaRepository;
    private final EstudianteRepository estudianteRepository;

    public AlertaService(AlertaRepository alertaRepository, EstudianteRepository estudianteRepository) {
        this.alertaRepository = alertaRepository;
        this.estudianteRepository = estudianteRepository;
    }

    public AlertasDTO obtener(Long estudianteId) {
        return armar(buscar(estudianteId));
    }

    @Transactional
    public AlertasDTO registrarEvaluacion(Long estudianteId, EvaluacionRequestDTO dto) {
        Estudiante estudiante = buscar(estudianteId);
        if (dto.snapshot() != null) {
            estudiante.setSnapshotAlineacion(dto.snapshot());
            estudianteRepository.save(estudiante);
        }
        Instant ahora = Instant.now();
        List<NuevaAlertaDTO> nuevas = dto.nuevas() == null ? List.of() : dto.nuevas();
        for (NuevaAlertaDTO nueva : nuevas) {
            alertaRepository.save(new Alerta(estudianteId, nueva.tipo(), nueva.texto(), ahora));
        }
        List<Alerta> todas = alertaRepository.findByEstudianteIdOrderByCreadaEnDescIdDesc(estudianteId);
        for (int i = MAXIMO_ALERTAS; i < todas.size(); i++) {
            alertaRepository.deleteById(todas.get(i).getId());
        }
        return armar(estudiante);
    }

    @Transactional
    public void marcarLeidas(Long estudianteId) {
        buscar(estudianteId);
        for (Alerta alerta : alertaRepository.findByEstudianteIdOrderByCreadaEnDescIdDesc(estudianteId)) {
            if (!alerta.isLeida()) {
                alerta.setLeida(true);
                alertaRepository.save(alerta);
            }
        }
    }

    @Transactional
    public void borrarTodas(Long estudianteId) {
        buscar(estudianteId);
        alertaRepository.deleteByEstudianteId(estudianteId);
    }

    private Estudiante buscar(Long estudianteId) {
        return estudianteRepository.findById(estudianteId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Estudiante no encontrado: " + estudianteId));
    }

    private AlertasDTO armar(Estudiante estudiante) {
        List<AlertaDTO> alertas = alertaRepository.findByEstudianteIdOrderByCreadaEnDescIdDesc(estudiante.getId()).stream()
                .limit(MAXIMO_ALERTAS)
                .map(a -> new AlertaDTO(a.getId(), a.getTipo(), a.getTexto(), a.getCreadaEn(), a.isLeida()))
                .toList();
        return new AlertasDTO(estudiante.getSnapshotAlineacion(), alertas);
    }
}
