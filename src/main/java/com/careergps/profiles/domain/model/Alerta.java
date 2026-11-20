package com.careergps.profiles.domain.model;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Alerta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "estudiante_id")
    private Long estudianteId;

    private String tipo;

    @Column(length = 300)
    private String texto;

    @Column(name = "creada_en")
    private Instant creadaEn;

    private boolean leida;

    public Alerta() {
    }

    public Alerta(Long estudianteId, String tipo, String texto, Instant creadaEn) {
        this.estudianteId = estudianteId;
        this.tipo = tipo;
        this.texto = texto;
        this.creadaEn = creadaEn;
    }

    public Long getId() {
        return id;
    }

    public Long getEstudianteId() {
        return estudianteId;
    }

    public String getTipo() {
        return tipo;
    }

    public String getTexto() {
        return texto;
    }

    public Instant getCreadaEn() {
        return creadaEn;
    }

    public boolean isLeida() {
        return leida;
    }

    public void setLeida(boolean leida) {
        this.leida = leida;
    }
}
