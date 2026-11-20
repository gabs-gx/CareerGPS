package com.careergps.profiles.application.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.careergps.profiles.application.dto.LoginRequestDTO;
import com.careergps.profiles.application.dto.RegistroRequestDTO;
import com.careergps.profiles.application.dto.SesionDTO;
import com.careergps.profiles.domain.model.Estudiante;
import com.careergps.profiles.domain.repository.EstudianteRepository;
import com.careergps.shared.domain.exception.CorreoYaRegistradoException;
import com.careergps.shared.domain.exception.CredencialesInvalidasException;

@Service
public class AuthService {

    private static final String MENSAJE_CREDENCIALES = "Correo o contraseña incorrectos";

    private final EstudianteRepository estudianteRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(EstudianteRepository estudianteRepository, PasswordEncoder passwordEncoder) {
        this.estudianteRepository = estudianteRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public SesionDTO registrar(RegistroRequestDTO dto) {
        String correo = normalizar(dto.correo());
        if (estudianteRepository.findByCorreo(correo).isPresent()) {
            throw new CorreoYaRegistradoException("Ya existe una cuenta con ese correo");
        }
        Estudiante estudiante = new Estudiante(dto.carrera().trim());
        estudiante.setNombres(dto.nombres().trim());
        estudiante.setApellidos(dto.apellidos().trim());
        estudiante.setCorreo(correo);
        estudiante.setPasswordHash(passwordEncoder.encode(dto.password()));
        return aSesion(estudianteRepository.save(estudiante));
    }

    public SesionDTO ingresar(LoginRequestDTO dto) {
        Estudiante estudiante = estudianteRepository.findByCorreo(normalizar(dto.correo()))
                .orElseThrow(() -> new CredencialesInvalidasException(MENSAJE_CREDENCIALES));
        if (estudiante.getPasswordHash() == null || !passwordEncoder.matches(dto.password(), estudiante.getPasswordHash())) {
            throw new CredencialesInvalidasException(MENSAJE_CREDENCIALES);
        }
        return aSesion(estudiante);
    }

    private String normalizar(String correo) {
        return correo.trim().toLowerCase();
    }

    private SesionDTO aSesion(Estudiante e) {
        return new SesionDTO(e.getId(), e.getNombres(), e.getApellidos(), e.getCorreo(), e.getCarrera());
    }
}
