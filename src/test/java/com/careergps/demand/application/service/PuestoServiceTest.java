package com.careergps.demand.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.careergps.demand.application.dto.DemandaDTO;
import com.careergps.demand.application.dto.PuestoRelacionadoDTO;
import com.careergps.demand.domain.model.FrecuenciaHabilidad;
import com.careergps.demand.domain.model.Puesto;
import com.careergps.demand.domain.repository.OfertaHabilidadRepository;
import com.careergps.demand.domain.repository.OfertaRepository;
import com.careergps.demand.domain.repository.PuestoRepository;
import com.careergps.shared.domain.exception.DatosInsuficientesException;

@ExtendWith(MockitoExtension.class)
class PuestoServiceTest {

    @Mock
    private PuestoRepository puestoRepository;
    @Mock
    private OfertaRepository ofertaRepository;
    @Mock
    private OfertaHabilidadRepository ofertaHabilidadRepository;

    @InjectMocks
    private PuestoService puestoService;

    private Puesto puesto;

    @BeforeEach
    void setUp() {
        puesto = new Puesto("Data Analyst");
        puesto.setId(10L);
    }

    @Test
    void calcularDemandaLanzaExcepcionSiHayMenosDeCincoOfertas() {
        when(puestoRepository.findById(10L)).thenReturn(Optional.of(puesto));
        when(ofertaRepository.countByPuestoId(10L)).thenReturn(2L);

        assertThatThrownBy(() -> puestoService.calcularDemanda(10L))
                .isInstanceOf(DatosInsuficientesException.class);
    }

    @Test
    void calcularDemandaCalculaFrecuenciaYPorcentajePorHabilidad() {
        when(puestoRepository.findById(10L)).thenReturn(Optional.of(puesto));
        when(ofertaRepository.countByPuestoId(10L)).thenReturn(5L);
        when(ofertaHabilidadRepository.contarFrecuenciaPorPuesto(10L)).thenReturn(List.of(
                new FrecuenciaHabilidad(2L, "Excel", 2L),
                new FrecuenciaHabilidad(1L, "SQL", 5L)));

        DemandaDTO demanda = puestoService.calcularDemanda(10L);

        assertThat(demanda.totalOfertas()).isEqualTo(5);
        assertThat(demanda.habilidades()).hasSize(2);
        assertThat(demanda.habilidades().get(0).nombre()).isEqualTo("SQL");

        DemandaDTO.HabilidadFrecuencia sql = demanda.habilidades().get(0);
        assertThat(sql.frecuencia()).isEqualTo(5);
        assertThat(sql.porcentaje()).isCloseTo(100.0, within(0.01));

        DemandaDTO.HabilidadFrecuencia excel = demanda.habilidades().get(1);
        assertThat(excel.frecuencia()).isEqualTo(2);
        assertThat(excel.porcentaje()).isCloseTo(40.0, within(0.01));
    }

    @Test
    void calcularSimilitudUsaIndiceDeJaccardSobreLasHabilidadesDeCadaPuesto() {
        when(ofertaHabilidadRepository.contarFrecuenciaPorPuesto(10L)).thenReturn(List.of(
                new FrecuenciaHabilidad(1L, "SQL", 1L), new FrecuenciaHabilidad(2L, "Excel", 1L)));
        when(ofertaHabilidadRepository.contarFrecuenciaPorPuesto(20L)).thenReturn(List.of(
                new FrecuenciaHabilidad(2L, "Excel", 1L), new FrecuenciaHabilidad(3L, "Power BI", 1L)));

        double similitud = puestoService.calcularSimilitud(10L, 20L);

        assertThat(similitud).isCloseTo(1.0 / 3.0, within(0.0001));
    }

    @Test
    void obtenerPuestosRelacionadosExcluyeElMismoPuestoYOrdenaDescendente() {
        Puesto biAnalyst = new Puesto("BI Analyst");
        biAnalyst.setId(20L);
        Puesto dataEngineer = new Puesto("Data Engineer");
        dataEngineer.setId(30L);

        when(puestoRepository.findById(10L)).thenReturn(Optional.of(puesto));
        when(puestoRepository.findAll()).thenReturn(List.of(puesto, biAnalyst, dataEngineer));

        when(ofertaHabilidadRepository.contarFrecuenciaPorPuesto(10L)).thenReturn(List.of(
                new FrecuenciaHabilidad(1L, "SQL", 1L), new FrecuenciaHabilidad(2L, "Excel", 1L)));
        when(ofertaHabilidadRepository.contarFrecuenciaPorPuesto(20L)).thenReturn(List.of(
                new FrecuenciaHabilidad(1L, "SQL", 1L), new FrecuenciaHabilidad(2L, "Excel", 1L)));
        when(ofertaHabilidadRepository.contarFrecuenciaPorPuesto(30L)).thenReturn(List.of(
                new FrecuenciaHabilidad(99L, "Docker", 1L)));

        List<PuestoRelacionadoDTO> relacionados = puestoService.obtenerPuestosRelacionados(10L);

        assertThat(relacionados).hasSize(1);
        assertThat(relacionados.get(0).puestoId()).isEqualTo(20L);
        assertThat(relacionados.get(0).similitud()).isCloseTo(1.0, within(0.0001));
    }
}
