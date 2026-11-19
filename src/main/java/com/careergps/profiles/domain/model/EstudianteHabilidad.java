package com.careergps.profiles.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class EstudianteHabilidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "estudiante_id")
    private Long estudianteId;

    @Column(name = "habilidad_id")
    private Long habilidadId;

    private String nivel;

    public EstudianteHabilidad() {
    }

    public EstudianteHabilidad(Long estudianteId, Long habilidadId, String nivel) {
        this.estudianteId = estudianteId;
        this.habilidadId = habilidadId;
        this.nivel = nivel;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getEstudianteId() {
        return estudianteId;
    }

    public void setEstudianteId(Long estudianteId) {
        this.estudianteId = estudianteId;
    }

    public Long getHabilidadId() {
        return habilidadId;
    }

    public void setHabilidadId(Long habilidadId) {
        this.habilidadId = habilidadId;
    }

    public String getNivel() {
        return nivel;
    }

    public void setNivel(String nivel) {
        this.nivel = nivel;
    }
}
