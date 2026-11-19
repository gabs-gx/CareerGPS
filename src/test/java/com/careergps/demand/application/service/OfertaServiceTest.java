package com.careergps.demand.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.careergps.demand.application.dto.OfertaRequestDTO;
import com.careergps.demand.domain.model.Habilidad;
import com.careergps.demand.domain.model.Oferta;
import com.careergps.demand.domain.model.OfertaHabilidad;
import com.careergps.demand.domain.model.Puesto;
import com.careergps.demand.domain.repository.HabilidadRepository;
import com.careergps.demand.domain.repository.OfertaHabilidadRepository;
import com.careergps.demand.domain.repository.OfertaRepository;
import com.careergps.demand.domain.repository.PuestoRepository;
import com.careergps.demand.domain.service.HabilidadDetector;
import com.careergps.shared.domain.exception.RecursoNoEncontradoException;

@ExtendWith(MockitoExtension.class)
class OfertaServiceTest {

    @Mock
    private OfertaRepository ofertaRepository;
    @Mock
    private OfertaHabilidadRepository ofertaHabilidadRepository;
    @Mock
    private PuestoRepository puestoRepository;
    @Mock
    private HabilidadRepository habilidadRepository;
    @Mock
    private HabilidadDetector habilidadDetector;

    @InjectMocks
    private OfertaService ofertaService;

    private void ofertaGuardadaConId(long id) {
        when(ofertaRepository.save(any(Oferta.class))).thenAnswer(invocacion -> {
            Oferta oferta = invocacion.getArgument(0);
            oferta.setId(id);
            return oferta;
        });
    }

    @Test
    void registrarOfertaGuardaLaOfertaYSusHabilidadesConfirmadas() {
        ofertaGuardadaConId(1L);
        when(puestoRepository.findById(10L)).thenReturn(Optional.of(new Puesto("Data Analyst")));
        when(habilidadRepository.findById(1L)).thenReturn(Optional.of(new Habilidad("SQL")));
        when(habilidadRepository.findById(2L)).thenReturn(Optional.of(new Habilidad("Excel")));
        OfertaRequestDTO dto = new OfertaRequestDTO("OF-DA-01", "Retail", 10L, "desc", List.of(1L, 2L));

        Oferta oferta = ofertaService.registrarOferta(dto);

        assertThat(oferta.getId()).isEqualTo(1L);
        assertThat(oferta.getCodigo()).isEqualTo("OF-DA-01");

        ArgumentCaptor<OfertaHabilidad> captor = ArgumentCaptor.forClass(OfertaHabilidad.class);
        verify(ofertaHabilidadRepository, times(2)).save(captor.capture());
        assertThat(captor.getAllValues())
                .extracting(OfertaHabilidad::getOfertaId, OfertaHabilidad::getHabilidadId)
                .containsExactlyInAnyOrder(
                        Tuple.tuple(1L, 1L),
                        Tuple.tuple(1L, 2L));
    }

    @Test
    void registrarOfertaFallaSiElPuestoNoExiste() {
        when(puestoRepository.findById(10L)).thenReturn(Optional.empty());
        OfertaRequestDTO dto = new OfertaRequestDTO("OF-DA-03", "Retail", 10L, "desc", List.of(1L));

        assertThatThrownBy(() -> ofertaService.registrarOferta(dto))
                .isInstanceOf(RecursoNoEncontradoException.class);
        verify(ofertaRepository, never()).save(any());
    }

    @Test
    void actualizarOfertaReemplazaSusDatosYSusHabilidades() {
        Oferta existente = new Oferta("OF-VIEJO", "Banca", 10L, "vieja");
        existente.setId(1L);
        when(ofertaRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(puestoRepository.findById(10L)).thenReturn(Optional.of(new Puesto("Data Analyst")));
        when(habilidadRepository.findById(3L)).thenReturn(Optional.of(new Habilidad("Power BI")));
        when(ofertaRepository.save(any(Oferta.class))).thenAnswer(invocacion -> invocacion.getArgument(0));

        Oferta actualizada = ofertaService.actualizarOferta(1L,
                new OfertaRequestDTO("OF-NUEVO", "Retail", 10L, "nueva", List.of(3L)));

        assertThat(actualizada.getCodigo()).isEqualTo("OF-NUEVO");
        assertThat(actualizada.getSector()).isEqualTo("Retail");
        assertThat(actualizada.getDescripcion()).isEqualTo("nueva");
        verify(ofertaHabilidadRepository).deleteByOfertaId(1L);
        verify(ofertaHabilidadRepository).save(any(OfertaHabilidad.class));
    }

    @Test
    void eliminarOfertaBorraSusHabilidadesYLaOferta() {
        Oferta existente = new Oferta("OF-DA-01", "Retail", 10L, "desc");
        existente.setId(1L);
        when(ofertaRepository.findById(1L)).thenReturn(Optional.of(existente));

        ofertaService.eliminarOferta(1L);

        verify(ofertaHabilidadRepository).deleteByOfertaId(1L);
        verify(ofertaRepository).deleteById(1L);
    }

}
