package com.careergps.demand.interfaces.rest;

import java.util.List;

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

import com.careergps.demand.application.dto.OfertaRequestDTO;
import com.careergps.demand.application.dto.SugerenciaRequestDTO;
import com.careergps.demand.application.service.OfertaService;
import com.careergps.demand.domain.model.Habilidad;
import com.careergps.demand.domain.model.Oferta;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/ofertas")
public class OfertaController {

    private final OfertaService ofertaService;

    public OfertaController(OfertaService ofertaService) {
        this.ofertaService = ofertaService;
    }

    @PostMapping("/sugerencias")
    public List<Habilidad> sugerirHabilidades(@Valid @RequestBody SugerenciaRequestDTO dto) {
        return ofertaService.sugerirHabilidades(dto.descripcion());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Oferta registrarOferta(@Valid @RequestBody OfertaRequestDTO dto) {
        return ofertaService.registrarOferta(dto);
    }

    @GetMapping
    public List<Oferta> listarOfertas() {
        return ofertaService.listarOfertas();
    }

    @GetMapping("/{id}")
    public Oferta obtenerOferta(@PathVariable Long id) {
        return ofertaService.obtenerOferta(id);
    }

    @PutMapping("/{id}")
    public Oferta actualizarOferta(@PathVariable Long id, @Valid @RequestBody OfertaRequestDTO dto) {
        return ofertaService.actualizarOferta(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminarOferta(@PathVariable Long id) {
        ofertaService.eliminarOferta(id);
    }
}
