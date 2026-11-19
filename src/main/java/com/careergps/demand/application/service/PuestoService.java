package com.careergps.demand.application.service;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.careergps.demand.application.dto.DemandaDTO;
import com.careergps.demand.application.dto.DemandaDTO.HabilidadFrecuencia;
import com.careergps.demand.application.dto.PuestoRelacionadoDTO;
import com.careergps.demand.domain.model.FrecuenciaHabilidad;
import com.careergps.demand.domain.model.Puesto;
import com.careergps.demand.domain.repository.OfertaHabilidadRepository;
import com.careergps.demand.domain.repository.OfertaRepository;
import com.careergps.demand.domain.repository.PuestoRepository;
import com.careergps.shared.domain.exception.DatosInsuficientesException;
import com.careergps.shared.domain.exception.RecursoNoEncontradoException;

@Service
public class PuestoService {

    // minimo de ofertas
    private static final int MINIMO_OFERTAS = 5;

    private final PuestoRepository puestoRepository;
    private final OfertaRepository ofertaRepository;
    private final OfertaHabilidadRepository ofertaHabilidadRepository;

    public PuestoService(
            PuestoRepository puestoRepository,
            OfertaRepository ofertaRepository,
            OfertaHabilidadRepository ofertaHabilidadRepository) {
        this.puestoRepository = puestoRepository;
        this.ofertaRepository = ofertaRepository;
        this.ofertaHabilidadRepository = ofertaHabilidadRepository;
    }

    public List<Puesto> listarPuestos() {
        return puestoRepository.findAll();
    }

    public DemandaDTO calcularDemanda(Long puestoId) {
        Puesto puesto = puestoRepository.findById(puestoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Puesto no encontrado: " + puestoId));

        long totalOfertas = ofertaRepository.countByPuestoId(puestoId);
        if (totalOfertas < MINIMO_OFERTAS) {
            throw new DatosInsuficientesException(
                    "Se necesitan al menos " + MINIMO_OFERTAS + " ofertas para calcular la demanda de este puesto");
        }

        List<HabilidadFrecuencia> habilidades = ofertaHabilidadRepository.contarFrecuenciaPorPuesto(puestoId).stream()
                .map(f -> new HabilidadFrecuencia(f.habilidadId(), f.nombre(), f.frecuencia(), f.frecuencia() * 100.0 / totalOfertas))
                .sorted(Comparator.comparingLong(HabilidadFrecuencia::frecuencia).reversed())
                .toList();

        return new DemandaDTO(puestoId, puesto.getNombre(), (int) totalOfertas, habilidades);
    }

    public double calcularSimilitud(Long puestoId1, Long puestoId2) {
        Set<Long> habilidadesA = habilidadesRequeridasPor(puestoId1);
        Set<Long> habilidadesB = habilidadesRequeridasPor(puestoId2);

        if (habilidadesA.isEmpty() && habilidadesB.isEmpty()) {
            return 0.0;
        }

        Set<Long> interseccion = new HashSet<>(habilidadesA);
        interseccion.retainAll(habilidadesB);

        Set<Long> union = new HashSet<>(habilidadesA);
        union.addAll(habilidadesB);

        return (double) interseccion.size() / union.size();
    }

    public List<PuestoRelacionadoDTO> obtenerPuestosRelacionados(Long puestoId) {
        puestoRepository.findById(puestoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Puesto no encontrado: " + puestoId));

        return puestoRepository.findAll().stream()
                .filter(otro -> !otro.getId().equals(puestoId))
                .map(otro -> new PuestoRelacionadoDTO(otro.getId(), otro.getNombre(), calcularSimilitud(puestoId, otro.getId())))
                .filter(dto -> dto.similitud() > 0)
                .sorted(Comparator.comparingDouble(PuestoRelacionadoDTO::similitud).reversed())
                .toList();
    }

    private Set<Long> habilidadesRequeridasPor(Long puestoId) {
        return ofertaHabilidadRepository.contarFrecuenciaPorPuesto(puestoId).stream()
                .map(FrecuenciaHabilidad::habilidadId)
                .collect(Collectors.toSet());
    }
}
