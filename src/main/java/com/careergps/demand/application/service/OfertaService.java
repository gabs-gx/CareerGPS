package com.careergps.demand.application.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.careergps.demand.application.dto.OfertaRequestDTO;
import com.careergps.demand.domain.model.Habilidad;
import com.careergps.demand.domain.model.Oferta;
import com.careergps.demand.domain.model.OfertaHabilidad;
import com.careergps.demand.domain.repository.HabilidadRepository;
import com.careergps.demand.domain.repository.OfertaHabilidadRepository;
import com.careergps.demand.domain.repository.OfertaRepository;
import com.careergps.demand.domain.repository.PuestoRepository;
import com.careergps.demand.domain.service.HabilidadDetector;
import com.careergps.shared.domain.exception.RecursoNoEncontradoException;

@Service
public class OfertaService {

    private final OfertaRepository ofertaRepository;
    private final OfertaHabilidadRepository ofertaHabilidadRepository;
    private final PuestoRepository puestoRepository;
    private final HabilidadRepository habilidadRepository;
    private final HabilidadDetector habilidadDetector;

    public OfertaService(
            OfertaRepository ofertaRepository,
            OfertaHabilidadRepository ofertaHabilidadRepository,
            PuestoRepository puestoRepository,
            HabilidadRepository habilidadRepository,
            HabilidadDetector habilidadDetector) {
        this.ofertaRepository = ofertaRepository;
        this.ofertaHabilidadRepository = ofertaHabilidadRepository;
        this.puestoRepository = puestoRepository;
        this.habilidadRepository = habilidadRepository;
        this.habilidadDetector = habilidadDetector;
    }

    public List<Habilidad> sugerirHabilidades(String descripcion) {
        return habilidadDetector.detectar(descripcion);
    }

    @Transactional
    public Oferta registrarOferta(OfertaRequestDTO dto) {
        validarReferencias(dto);

        Oferta oferta = ofertaRepository.save(new Oferta(dto.codigo(), dto.sector(), dto.puestoId(), dto.descripcion()));
        guardarHabilidades(oferta.getId(), dto.habilidadIdsConfirmadas());
        return oferta;
    }

    public List<Oferta> listarOfertas() {
        return ofertaRepository.findAll();
    }

    public Oferta obtenerOferta(Long id) {
        return ofertaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Oferta no encontrada: " + id));
    }

    @Transactional
    public Oferta actualizarOferta(Long id, OfertaRequestDTO dto) {
        Oferta oferta = obtenerOferta(id);
        validarReferencias(dto);

        oferta.setCodigo(dto.codigo());
        oferta.setSector(dto.sector());
        oferta.setPuestoId(dto.puestoId());
        oferta.setDescripcion(dto.descripcion());
        oferta = ofertaRepository.save(oferta);

        ofertaHabilidadRepository.deleteByOfertaId(id);
        guardarHabilidades(id, dto.habilidadIdsConfirmadas());
        return oferta;
    }

    @Transactional
    public void eliminarOferta(Long id) {
        obtenerOferta(id);
        ofertaHabilidadRepository.deleteByOfertaId(id);
        ofertaRepository.deleteById(id);
    }

    private void validarReferencias(OfertaRequestDTO dto) {
        puestoRepository.findById(dto.puestoId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Rol no encontrado: " + dto.puestoId()));
        if (dto.habilidadIdsConfirmadas() != null) {
            for (Long habilidadId : dto.habilidadIdsConfirmadas()) {
                habilidadRepository.findById(habilidadId)
                        .orElseThrow(() -> new RecursoNoEncontradoException("Habilidad no encontrada: " + habilidadId));
            }
        }
    }

    private void guardarHabilidades(Long ofertaId, List<Long> habilidadIds) {
        List<Long> ids = habilidadIds != null ? habilidadIds : List.of();
        for (Long habilidadId : ids) {
            ofertaHabilidadRepository.save(new OfertaHabilidad(ofertaId, habilidadId));
        }
    }
}
