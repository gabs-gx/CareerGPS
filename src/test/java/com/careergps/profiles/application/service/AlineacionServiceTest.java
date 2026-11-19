package com.careergps.profiles.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.careergps.demand.application.dto.DemandaDTO;
import com.careergps.demand.application.dto.DemandaDTO.HabilidadFrecuencia;
import com.careergps.demand.application.service.PuestoService;
import com.careergps.profiles.domain.model.EstudianteHabilidad;
import com.careergps.profiles.domain.repository.EstudianteHabilidadRepository;
import com.careergps.profiles.domain.repository.EstudianteRepository;

@ExtendWith(MockitoExtension.class)
class AlineacionServiceTest {

    @Mock
    private EstudianteRepository estudianteRepository;
    @Mock
    private EstudianteHabilidadRepository estudianteHabilidadRepository;
    @Mock
    private PuestoService puestoService;

    @InjectMocks
    private AlineacionService alineacionService;

    @Test
    void calcularAlineacionSeparaCoincidenciasDeHabilidadesAFortalecer() {
        DemandaDTO demanda = new DemandaDTO(10L, "Data Analyst", 5, List.of(
                new HabilidadFrecuencia(1L, "SQL", 5, 100.0),
                new HabilidadFrecuencia(3L, "Power BI", 2, 40.0)));
        when(estudianteRepository.existsById(100L)).thenReturn(true);
        when(puestoService.calcularDemanda(10L)).thenReturn(demanda);
        when(estudianteHabilidadRepository.findByEstudianteId(100L))
                .thenReturn(List.of(new EstudianteHabilidad(100L, 1L, "dominado")));

        var alineacion = alineacionService.calcularAlineacion(100L, 10L);

        assertThat(alineacion.coincidencias()).containsExactly("SQL");
        assertThat(alineacion.aFortalecer()).containsExactly("Power BI");
    }

    @Test
    void calcularAlineacionIgnoraLasHabilidadesDeclaradasComoNoTiene() {
        DemandaDTO demanda = new DemandaDTO(10L, "Data Analyst", 5, List.of(
                new HabilidadFrecuencia(1L, "SQL", 5, 100.0)));
        when(estudianteRepository.existsById(100L)).thenReturn(true);
        when(puestoService.calcularDemanda(10L)).thenReturn(demanda);
        when(estudianteHabilidadRepository.findByEstudianteId(100L))
                .thenReturn(List.of(new EstudianteHabilidad(100L, 1L, "no tiene")));

        var alineacion = alineacionService.calcularAlineacion(100L, 10L);

        assertThat(alineacion.coincidencias()).isEmpty();
        assertThat(alineacion.aFortalecer()).containsExactly("SQL");
    }
}
