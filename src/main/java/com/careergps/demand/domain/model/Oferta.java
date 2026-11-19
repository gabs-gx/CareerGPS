package com.careergps.demand.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;

@Entity
public class Oferta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String codigo;

    private String sector;

    @Column(name = "puesto_id")
    private Long puestoId;

    @Lob
    private String descripcion;

    public Oferta() {
    }

    public Oferta(String codigo, String sector, Long puestoId, String descripcion) {
        this.codigo = codigo;
        this.sector = sector;
        this.puestoId = puestoId;
        this.descripcion = descripcion;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getSector() {
        return sector;
    }

    public void setSector(String sector) {
        this.sector = sector;
    }

    public Long getPuestoId() {
        return puestoId;
    }

    public void setPuestoId(Long puestoId) {
        this.puestoId = puestoId;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
}
