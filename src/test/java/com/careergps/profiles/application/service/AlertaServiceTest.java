package com.careergps.profiles.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.careergps.profiles.application.dto.AlertasDTO;
import com.careergps.profiles.application.dto.EvaluacionRequestDTO;
import com.careergps.profiles.application.dto.NuevaAlertaDTO;
import com.careergps.profiles.domain.model.Alerta;
import com.careergps.profiles.domain.model.Estudiante;
import com.careergps.profiles.domain.repository.AlertaRepository;
import com.careergps.profiles.domain.repository.EstudianteRepository;
import com.careergps.shared.domain.exception.RecursoNoEncontradoException;

@ExtendWith(MockitoExtension.class)
class AlertaServiceTest {

    @Mock
    private AlertaRepository alertaRepository;
    @Mock
    private EstudianteRepository estudianteRepository;

    @InjectMocks
    private AlertaService alertaService;

    private Estudiante estudiante;

    @BeforeEach
    void preparar() {
        estudiante = new Estudiante("Sistemas");
        estudiante.setId(5L);
    }

    @Test
    void registrarEvaluacionGuardaElResumenYLasAlertasNuevas() {
        when(estudianteRepository.findById(5L)).thenReturn(Optional.of(estudiante));
        when(alertaRepository.findByEstudianteIdOrderByCreadaEnDescIdDesc(5L)).thenReturn(List.of());

        alertaService.registrarEvaluacion(5L, new EvaluacionRequestDTO("{\"1\":{\"pct\":40}}",
                List.of(new NuevaAlertaDTO("sube", "Tu alineacion subio"), new NuevaAlertaDTO("meta", "Llegaste al 50%"))));

        assertThat(estudiante.getSnapshotAlineacion()).isEqualTo("{\"1\":{\"pct\":40}}");
        ArgumentCaptor<Alerta> guardadas = ArgumentCaptor.forClass(Alerta.class);
        verify(alertaRepository, times(2)).save(guardadas.capture());
        assertThat(guardadas.getAllValues()).extracting(Alerta::getTipo).containsExactly("sube", "meta");
        assertThat(guardadas.getAllValues()).allMatch(a -> a.getEstudianteId().equals(5L) && !a.isLeida());
    }

    @Test
    void registrarEvaluacionConservaSoloLas30AlertasMasRecientes() {
        when(estudianteRepository.findById(5L)).thenReturn(Optional.of(estudiante));
        List<Alerta> existentes = new ArrayList<>();
        for (int i = 0; i < 32; i++) {
            existentes.add(new Alerta(5L, "info", "alerta " + i, Instant.now()));
        }
        when(alertaRepository.findByEstudianteIdOrderByCreadaEnDescIdDesc(5L)).thenReturn(existentes);

        alertaService.registrarEvaluacion(5L, new EvaluacionRequestDTO(null, List.of()));

        verify(alertaRepository, times(2)).deleteById(any());
    }

    @Test
    void marcarLeidasActualizaSoloLasPendientes() {
        when(estudianteRepository.findById(5L)).thenReturn(Optional.of(estudiante));
        Alerta pendiente = new Alerta(5L, "info", "nueva", Instant.now());
        Alerta leida = new Alerta(5L, "info", "vieja", Instant.now());
        leida.setLeida(true);
        when(alertaRepository.findByEstudianteIdOrderByCreadaEnDescIdDesc(5L)).thenReturn(List.of(pendiente, leida));

        alertaService.marcarLeidas(5L);

        assertThat(pendiente.isLeida()).isTrue();
        verify(alertaRepository, times(1)).save(pendiente);
        verify(alertaRepository, never()).save(leida);
    }

    @Test
    void borrarTodasEliminaLasAlertasDelEstudiante() {
        when(estudianteRepository.findById(5L)).thenReturn(Optional.of(estudiante));

        alertaService.borrarTodas(5L);

        verify(alertaRepository).deleteByEstudianteId(5L);
    }

    @Test
    void obtenerDevuelveElResumenYLasAlertas() {
        estudiante.setSnapshotAlineacion("{}");
        when(estudianteRepository.findById(5L)).thenReturn(Optional.of(estudiante));
        when(alertaRepository.findByEstudianteIdOrderByCreadaEnDescIdDesc(5L))
                .thenReturn(List.of(new Alerta(5L, "baja", "bajo", Instant.now())));

        AlertasDTO resultado = alertaService.obtener(5L);

        assertThat(resultado.snapshot()).isEqualTo("{}");
        assertThat(resultado.alertas()).hasSize(1);
        assertThat(resultado.alertas().get(0).tipo()).isEqualTo("baja");
    }

    @Test
    void conUnEstudianteInexistenteLanzaNoEncontrado() {
        when(estudianteRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> alertaService.obtener(99L)).isInstanceOf(RecursoNoEncontradoException.class);
    }
}
