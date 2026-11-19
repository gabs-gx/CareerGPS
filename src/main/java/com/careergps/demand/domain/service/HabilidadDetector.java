package com.careergps.demand.domain.service;

import java.text.Normalizer;
import java.util.List;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

import com.careergps.demand.domain.model.Habilidad;
import com.careergps.demand.domain.repository.HabilidadRepository;

// detecta habilidades en un texto
@Component
public class HabilidadDetector {

    private static final Pattern NO_ALFANUMERICO = Pattern.compile("[^a-z0-9]+");

    private final HabilidadRepository habilidadRepository;

    public HabilidadDetector(HabilidadRepository habilidadRepository) {
        this.habilidadRepository = habilidadRepository;
    }

    public String normalizar(String texto) {
        String sinTildes = Normalizer.normalize(texto.toLowerCase(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return NO_ALFANUMERICO.matcher(sinTildes).replaceAll(" ").trim().replaceAll(" +", " ");
    }

    public List<Habilidad> detectar(String descripcion) {
        String textoNormalizado = " " + normalizar(descripcion) + " ";

        return habilidadRepository.findAll().stream()
                .filter(habilidad -> textoNormalizado.contains(" " + normalizar(habilidad.getNombre()) + " "))
                .toList();
    }
}
