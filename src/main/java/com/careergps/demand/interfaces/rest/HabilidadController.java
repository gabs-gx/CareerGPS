package com.careergps.demand.interfaces.rest;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.careergps.demand.domain.model.Habilidad;
import com.careergps.demand.application.service.HabilidadService;

@RestController
@RequestMapping("/api/habilidades")
public class HabilidadController {

    private final HabilidadService habilidadService;

    public HabilidadController(HabilidadService habilidadService) {
        this.habilidadService = habilidadService;
    }

    @GetMapping
    public List<Habilidad> listarHabilidades() {
        return habilidadService.listarHabilidades();
    }
}
