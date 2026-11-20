package com.careergps.profiles.interfaces.rest;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.careergps.profiles.application.dto.LoginRequestDTO;
import com.careergps.profiles.application.dto.RegistroRequestDTO;
import com.careergps.profiles.application.dto.SesionDTO;
import com.careergps.profiles.application.service.AuthService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/registro")
    @ResponseStatus(HttpStatus.CREATED)
    public SesionDTO registrar(@Valid @RequestBody RegistroRequestDTO dto) {
        return authService.registrar(dto);
    }

    @PostMapping("/login")
    public SesionDTO ingresar(@Valid @RequestBody LoginRequestDTO dto) {
        return authService.ingresar(dto);
    }
}
