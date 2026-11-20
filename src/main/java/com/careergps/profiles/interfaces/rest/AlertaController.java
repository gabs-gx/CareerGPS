package com.careergps.profiles.interfaces.rest;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.careergps.profiles.application.dto.AlertasDTO;
import com.careergps.profiles.application.dto.EvaluacionRequestDTO;
import com.careergps.profiles.application.service.AlertaService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/estudiantes/{id}/alertas")
public class AlertaController {

    private final AlertaService alertaService;

    public AlertaController(AlertaService alertaService) {
        this.alertaService = alertaService;
    }

    @GetMapping
    public AlertasDTO obtener(@PathVariable Long id) {
        return alertaService.obtener(id);
    }

    @PostMapping
    public AlertasDTO registrarEvaluacion(@PathVariable Long id, @Valid @RequestBody EvaluacionRequestDTO dto) {
        return alertaService.registrarEvaluacion(id, dto);
    }

    @PutMapping("/leidas")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void marcarLeidas(@PathVariable Long id) {
        alertaService.marcarLeidas(id);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void borrarTodas(@PathVariable Long id) {
        alertaService.borrarTodas(id);
    }
}
