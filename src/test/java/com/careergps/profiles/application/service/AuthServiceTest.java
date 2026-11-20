package com.careergps.profiles.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.careergps.profiles.application.dto.LoginRequestDTO;
import com.careergps.profiles.application.dto.RegistroRequestDTO;
import com.careergps.profiles.application.dto.SesionDTO;
import com.careergps.profiles.domain.model.Estudiante;
import com.careergps.profiles.domain.repository.EstudianteRepository;
import com.careergps.shared.domain.exception.CorreoYaRegistradoException;
import com.careergps.shared.domain.exception.CredencialesInvalidasException;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private EstudianteRepository estudianteRepository;

    private final PasswordEncoder encoder = new BCryptPasswordEncoder();
    private AuthService authService;

    @BeforeEach
    void preparar() {
        authService = new AuthService(estudianteRepository, encoder);
    }

    private RegistroRequestDTO registro(String correo) {
        return new RegistroRequestDTO(" Ana ", "Perez", correo, "clave-segura-1", "Ingenieria de Sistemas");
    }

    @Test
    void registrarGuardaElCorreoEnMinusculasYLaContrasenaCifrada() {
        when(estudianteRepository.findByCorreo("ana@correo.com")).thenReturn(Optional.empty());
        when(estudianteRepository.save(any(Estudiante.class))).thenAnswer(inv -> {
            Estudiante e = inv.getArgument(0);
            e.setId(7L);
            return e;
        });

        SesionDTO sesion = authService.registrar(registro("  Ana@Correo.com "));

        ArgumentCaptor<Estudiante> guardado = ArgumentCaptor.forClass(Estudiante.class);
        verify(estudianteRepository).save(guardado.capture());
        assertThat(guardado.getValue().getCorreo()).isEqualTo("ana@correo.com");
        assertThat(guardado.getValue().getNombres()).isEqualTo("Ana");
        assertThat(guardado.getValue().getPasswordHash()).isNotEqualTo("clave-segura-1");
        assertThat(encoder.matches("clave-segura-1", guardado.getValue().getPasswordHash())).isTrue();
        assertThat(sesion.id()).isEqualTo(7L);
        assertThat(sesion.correo()).isEqualTo("ana@correo.com");
    }

    @Test
    void registrarConUnCorreoYaUsadoLanzaConflicto() {
        when(estudianteRepository.findByCorreo("ana@correo.com")).thenReturn(Optional.of(new Estudiante()));

        assertThatThrownBy(() -> authService.registrar(registro("ana@correo.com")))
                .isInstanceOf(CorreoYaRegistradoException.class);
        verify(estudianteRepository, never()).save(any());
    }

    @Test
    void ingresarConLasCredencialesCorrectasDevuelveLaSesion() {
        Estudiante existente = new Estudiante("Economia");
        existente.setId(3L);
        existente.setNombres("Ana");
        existente.setApellidos("Perez");
        existente.setCorreo("ana@correo.com");
        existente.setPasswordHash(encoder.encode("clave-segura-1"));
        when(estudianteRepository.findByCorreo("ana@correo.com")).thenReturn(Optional.of(existente));

        SesionDTO sesion = authService.ingresar(new LoginRequestDTO("ANA@correo.com", "clave-segura-1"));

        assertThat(sesion.id()).isEqualTo(3L);
        assertThat(sesion.carrera()).isEqualTo("Economia");
    }

    @Test
    void ingresarConContrasenaIncorrectaLanzaCredencialesInvalidas() {
        Estudiante existente = new Estudiante("Economia");
        existente.setPasswordHash(encoder.encode("clave-segura-1"));
        when(estudianteRepository.findByCorreo("ana@correo.com")).thenReturn(Optional.of(existente));

        assertThatThrownBy(() -> authService.ingresar(new LoginRequestDTO("ana@correo.com", "otra-clave")))
                .isInstanceOf(CredencialesInvalidasException.class);
    }

    @Test
    void ingresarConUnCorreoInexistenteLanzaElMismoError() {
        when(estudianteRepository.findByCorreo("nadie@correo.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.ingresar(new LoginRequestDTO("nadie@correo.com", "clave-segura-1")))
                .isInstanceOf(CredencialesInvalidasException.class);
    }
}
