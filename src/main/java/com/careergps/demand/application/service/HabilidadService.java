package com.careergps.demand.application.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.careergps.demand.domain.model.Habilidad;
import com.careergps.demand.domain.repository.HabilidadRepository;

@Service
public class HabilidadService {

    private final HabilidadRepository habilidadRepository;

    public HabilidadService(HabilidadRepository habilidadRepository) {
        this.habilidadRepository = habilidadRepository;
    }

    public List<Habilidad> listarHabilidades() {
        return habilidadRepository.findAll();
    }
}
