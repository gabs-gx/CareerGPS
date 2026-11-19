package com.careergps.demand.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.careergps.demand.domain.model.Habilidad;
import com.careergps.demand.domain.repository.HabilidadRepository;

@ExtendWith(MockitoExtension.class)
class HabilidadDetectorTest {

    @Mock
    private HabilidadRepository habilidadRepository;

    private HabilidadDetector detector;

    @BeforeEach
    void setUp() {
        detector = new HabilidadDetector(habilidadRepository);
    }

    @Test
    void normalizarQuitaTildesMayusculasYCaracteresEspeciales() {
        assertThat(detector.normalizar("Power-BI")).isEqualTo("power bi");
        assertThat(detector.normalizar("Programación")).isEqualTo("programacion");
        assertThat(detector.normalizar("  SQL  ")).isEqualTo("sql");
    }

    @Test
    void detectarReconoceVariantesDeEscrituraDeUnaMismaHabilidad() {
        when(habilidadRepository.findAll()).thenReturn(List.of(
                new Habilidad("Power BI"),
                new Habilidad("Excel"),
                new Habilidad("Java")));

        List<Habilidad> detectadas = detector
                .detectar("Se requiere experiencia en Power-BI y en Excel avanzado.");

        assertThat(detectadas).extracting(Habilidad::getNombre)
                .containsExactlyInAnyOrder("Power BI", "Excel");
    }

    @Test
    void detectarNoConfundeHabilidadesQueSonSubcadenaDeOtraPalabra() {
        when(habilidadRepository.findAll()).thenReturn(List.of(
                new Habilidad("Java"),
                new Habilidad("JavaScript")));

        List<Habilidad> detectadas = detector.detectar("Se busca desarrollador con experiencia en JavaScript.");

        assertThat(detectadas).extracting(Habilidad::getNombre)
                .containsExactly("JavaScript");
    }
}
