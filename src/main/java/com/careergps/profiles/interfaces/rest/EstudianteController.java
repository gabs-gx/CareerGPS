package com.careergps.profiles.interfaces.rest;

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

import com.careergps.profiles.application.dto.AlineacionDTO;
import com.careergps.profiles.application.dto.EstudianteHabilidadRequestDTO;
import com.careergps.profiles.application.dto.EstudianteRequestDTO;
import com.careergps.profiles.application.service.AlineacionService;
import com.careergps.profiles.application.service.EstudianteService;
import com.careergps.profiles.domain.model.Estudiante;
import com.careergps.profiles.domain.model.EstudianteHabilidad;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/estudiantes")
public class EstudianteController {

    private final EstudianteService estudianteService;
    private final AlineacionService alineacionService;

    public EstudianteController(EstudianteService estudianteService, AlineacionService alineacionService) {
        this.estudianteService = estudianteService;
        this.alineacionService = alineacionService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Estudiante registrarPerfil(@Valid @RequestBody EstudianteRequestDTO dto) {
        return estudianteService.registrarPerfil(dto);
    }

    @GetMapping("/{id}")
    public Estudiante obtenerPerfil(@PathVariable Long id) {
        return estudianteService.obtenerPerfil(id);
    }

    @PutMapping("/{id}")
    public Estudiante actualizarPerfil(@PathVariable Long id, @Valid @RequestBody EstudianteRequestDTO dto) {
        return estudianteService.actualizarPerfil(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminarPerfil(@PathVariable Long id) {
        estudianteService.eliminarPerfil(id);
    }

    @PostMapping("/{id}/habilidades")
    @ResponseStatus(HttpStatus.CREATED)
    public EstudianteHabilidad registrarHabilidad(@PathVariable Long id, @Valid @RequestBody EstudianteHabilidadRequestDTO dto) {
        return estudianteService.registrarHabilidad(id, dto);
    }

    @GetMapping("/{id}/habilidades")
    public List<EstudianteHabilidad> listarHabilidades(@PathVariable Long id) {
        return estudianteService.listarHabilidades(id);
    }

    @GetMapping("/{id}/alineacion/{puestoId}")
    public AlineacionDTO consultarAlineacion(@PathVariable Long id, @PathVariable Long puestoId) {
        return alineacionService.calcularAlineacion(id, puestoId);
    }
}
