package com.careergps.shared.infrastructure;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.careergps.demand.domain.model.Habilidad;
import com.careergps.demand.domain.model.Oferta;
import com.careergps.demand.domain.model.OfertaHabilidad;
import com.careergps.demand.domain.model.Puesto;
import com.careergps.demand.domain.repository.HabilidadRepository;
import com.careergps.demand.domain.repository.OfertaHabilidadRepository;
import com.careergps.demand.domain.repository.OfertaRepository;
import com.careergps.demand.domain.repository.PuestoRepository;
import com.careergps.profiles.domain.model.Estudiante;
import com.careergps.profiles.domain.model.EstudianteHabilidad;
import com.careergps.profiles.domain.repository.EstudianteHabilidadRepository;
import com.careergps.profiles.domain.repository.EstudianteRepository;

// datos de prueba
@Component
public class DataSeeder implements CommandLineRunner {

    private static final String[] SECTORES = { "Retail", "Banca", "Tecnologia", "Salud", "Consultoria" };

    private final PuestoRepository puestoRepository;
    private final HabilidadRepository habilidadRepository;
    private final OfertaRepository ofertaRepository;
    private final OfertaHabilidadRepository ofertaHabilidadRepository;
    private final EstudianteRepository estudianteRepository;
    private final EstudianteHabilidadRepository estudianteHabilidadRepository;

    public DataSeeder(
            PuestoRepository puestoRepository,
            HabilidadRepository habilidadRepository,
            OfertaRepository ofertaRepository,
            OfertaHabilidadRepository ofertaHabilidadRepository,
            EstudianteRepository estudianteRepository,
            EstudianteHabilidadRepository estudianteHabilidadRepository) {
        this.puestoRepository = puestoRepository;
        this.habilidadRepository = habilidadRepository;
        this.ofertaRepository = ofertaRepository;
        this.ofertaHabilidadRepository = ofertaHabilidadRepository;
        this.estudianteRepository = estudianteRepository;
        this.estudianteHabilidadRepository = estudianteHabilidadRepository;
    }

    @Override
    public void run(String... args) {
        if (!puestoRepository.findAll().isEmpty() || !habilidadRepository.findAll().isEmpty()) {
            return;
        }

        Map<String, Habilidad> habilidades = new HashMap<>();
        for (String nombre : List.of("SQL", "Excel", "Power BI", "Python", "Tableau", "Estadistica", "Machine Learning", "Git", "Docker", "Spark", "AWS")) {
            habilidades.put(nombre, habilidadRepository.save(new Habilidad(nombre)));
        }

        Puesto dataAnalyst = puestoRepository.save(new Puesto("Data Analyst"));
        Puesto dataScientist = puestoRepository.save(new Puesto("Data Scientist"));
        Puesto dataEngineer = puestoRepository.save(new Puesto("Data Engineer"));

        cargarOfertas("DA", dataAnalyst, habilidades, List.of(
                List.of("SQL", "Excel", "Power BI", "Python"),
                List.of("SQL", "Excel", "Power BI", "Python"),
                List.of("SQL", "Excel", "Power BI", "Python"),
                List.of("SQL", "Excel", "Power BI", "Python"),
                List.of("SQL", "Excel", "Power BI", "Python"),
                List.of("SQL", "Excel", "Power BI"),
                List.of("SQL", "Excel", "Power BI"),
                List.of("SQL", "Excel"),
                List.of("SQL"),
                List.of("Tableau")));

        cargarOfertas("DS", dataScientist, habilidades, List.of(
                List.of("Python", "SQL", "Estadistica", "Machine Learning"),
                List.of("Python", "SQL", "Estadistica", "Machine Learning"),
                List.of("Python", "Estadistica", "Machine Learning", "Git"),
                List.of("Python", "SQL", "Machine Learning"),
                List.of("Python", "Estadistica", "Machine Learning"),
                List.of("Python", "SQL", "Estadistica"),
                List.of("Python", "Machine Learning", "Git"),
                List.of("Python", "SQL", "Machine Learning", "AWS")));

        cargarOfertas("DE", dataEngineer, habilidades, List.of(
                List.of("Python", "SQL", "Docker"),
                List.of("Python", "SQL", "Git", "Spark"),
                List.of("Python", "Docker", "Git")));

        Estudiante estudiante = estudianteRepository.save(new Estudiante("Ingenieria de Sistemas"));
        estudianteHabilidadRepository.save(new EstudianteHabilidad(estudiante.getId(), habilidades.get("SQL").getId(), "dominado"));
        estudianteHabilidadRepository.save(new EstudianteHabilidad(estudiante.getId(), habilidades.get("Excel").getId(), "basico"));
        estudianteHabilidadRepository.save(new EstudianteHabilidad(estudiante.getId(), habilidades.get("Python").getId(), "no tiene"));
    }

    private void cargarOfertas(String prefijo, Puesto puesto, Map<String, Habilidad> habilidades, List<List<String>> habilidadesPorOferta) {
        for (int i = 0; i < habilidadesPorOferta.size(); i++) {
            List<String> pedidas = habilidadesPorOferta.get(i);
            String codigo = String.format("OF-%s-%02d", prefijo, i + 1);
            String descripcion = "Se busca " + puesto.getNombre() + " con experiencia en " + String.join(", ", pedidas) + ".";

            Oferta oferta = ofertaRepository.save(new Oferta(codigo, SECTORES[i % SECTORES.length], puesto.getId(), descripcion));
            for (String nombre : pedidas) {
                ofertaHabilidadRepository.save(new OfertaHabilidad(oferta.getId(), habilidades.get(nombre).getId()));
            }
        }
    }
}
