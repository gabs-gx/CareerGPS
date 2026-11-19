package com.careergps.profiles.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.careergps.demand.domain.model.Habilidad;
import com.careergps.demand.domain.repository.HabilidadRepository;
import com.careergps.profiles.application.dto.EstudianteHabilidadRequestDTO;
import com.careergps.profiles.application.dto.EstudianteRequestDTO;
import com.careergps.profiles.domain.model.Estudiante;
import com.careergps.profiles.domain.model.EstudianteHabilidad;
import com.careergps.profiles.domain.repository.EstudianteHabilidadRepository;
import com.careergps.profiles.domain.repository.EstudianteRepository;

@ExtendWith(MockitoExtension.class)
class EstudianteServiceTest {

    @Mock
    private EstudianteRepository estudianteRepository;
    @Mock
    private HabilidadRepository habilidadRepository;
    @Mock
    private EstudianteHabilidadRepository estudianteHabilidadRepository;

    @InjectMocks
    private EstudianteService estudianteService;

    @Test
    void registrarPerfilGuardaElEstudiante() {
        when(estudianteRepository.save(any(Estudiante.class))).thenAnswer(invocacion -> {
            Estudiante estudiante = invocacion.getArgument(0);
            estudiante.setId(1L);
            return estudiante;
        });

        Estudiante estudiante = estudianteService.registrarPerfil(new EstudianteRequestDTO("Ingenieria de Sistemas"));

        assertThat(estudiante.getId()).isEqualTo(1L);
        assertThat(estudiante.getCarrera()).isEqualTo("Ingenieria de Sistemas");
    }

    @Test
    void actualizarPerfilCambiaLaCarrera() {
        Estudiante existente = new Estudiante("Sistemas");
        existente.setId(1L);
        when(estudianteRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(estudianteRepository.save(any(Estudiante.class))).thenAnswer(invocacion -> invocacion.getArgument(0));

        Estudiante actualizado = estudianteService.actualizarPerfil(1L, new EstudianteRequestDTO("Estadistica"));

        assertThat(actualizado.getCarrera()).isEqualTo("Estadistica");
    }

    @Test
    void eliminarPerfilBorraSusHabilidadesYAlEstudiante() {
        Estudiante existente = new Estudiante("Sistemas");
        existente.setId(1L);
        when(estudianteRepository.findById(1L)).thenReturn(Optional.of(existente));

        estudianteService.eliminarPerfil(1L);

        verify(estudianteHabilidadRepository).deleteByEstudianteId(1L);
        verify(estudianteRepository).deleteById(1L);
    }

    @Test
    void registrarHabilidadGuardaLaRelacionConElNivelDeclarado() {
        Estudiante estudiante = new Estudiante("Sistemas");
        estudiante.setId(1L);
        Habilidad sql = new Habilidad("SQL");
        sql.setId(5L);
        when(estudianteRepository.findById(1L)).thenReturn(Optional.of(estudiante));
        when(habilidadRepository.findById(5L)).thenReturn(Optional.of(sql));
        when(estudianteHabilidadRepository.save(any(EstudianteHabilidad.class)))
                .thenAnswer(invocacion -> invocacion.getArgument(0));

        EstudianteHabilidad resultado = estudianteService.registrarHabilidad(1L, new EstudianteHabilidadRequestDTO(5L, "dominado"));

        assertThat(resultado.getEstudianteId()).isEqualTo(1L);
        assertThat(resultado.getHabilidadId()).isEqualTo(5L);
        assertThat(resultado.getNivel()).isEqualTo("dominado");
    }

}
