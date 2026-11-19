package com.careergps.demand.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class OfertaHabilidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "oferta_id")
    private Long ofertaId;

    @Column(name = "habilidad_id")
    private Long habilidadId;

    public OfertaHabilidad() {
    }

    public OfertaHabilidad(Long ofertaId, Long habilidadId) {
        this.ofertaId = ofertaId;
        this.habilidadId = habilidadId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getOfertaId() {
        return ofertaId;
    }

    public void setOfertaId(Long ofertaId) {
        this.ofertaId = ofertaId;
    }

    public Long getHabilidadId() {
        return habilidadId;
    }

    public void setHabilidadId(Long habilidadId) {
        this.habilidadId = habilidadId;
    }
}
