package com.careergps.demand.interfaces.rest;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.careergps.demand.application.dto.DemandaDTO;
import com.careergps.demand.application.dto.PuestoRelacionadoDTO;
import com.careergps.demand.application.dto.PuestoResumenDTO;
import com.careergps.demand.domain.model.Puesto;
import com.careergps.demand.application.service.PuestoService;

@RestController
@RequestMapping("/api/puestos")
public class PuestoController {

    private final PuestoService puestoService;

    public PuestoController(PuestoService puestoService) {
        this.puestoService = puestoService;
    }

    @GetMapping
    public List<Puesto> listarPuestos() {
        return puestoService.listarPuestos();
    }

    @GetMapping("/resumen")
    public List<PuestoResumenDTO> listarResumen() {
        return puestoService.listarResumen();
    }

    @GetMapping("/{id}/demanda")
    public DemandaDTO consultarDemanda(@PathVariable Long id) {
        return puestoService.calcularDemanda(id);
    }

    @GetMapping("/{id}/relacionados")
    public List<PuestoRelacionadoDTO> consultarPuestosRelacionados(@PathVariable Long id) {
        return puestoService.obtenerPuestosRelacionados(id);
    }
}
