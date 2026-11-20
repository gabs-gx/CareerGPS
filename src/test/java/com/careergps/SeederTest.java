package com.careergps;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import com.careergps.demand.domain.model.Oferta;
import com.careergps.demand.domain.repository.OfertaRepository;
import com.careergps.shared.infrastructure.DataSeeder;

@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class SeederTest {

    @Autowired
    private DataSeeder seeder;
    @Autowired
    private OfertaRepository ofertaRepository;

    @Test
    void repiteLaCargaSinDuplicarYCompletaLosPuestosIncompletos() {
        assertThat(ofertaRepository.countByPuestoId(2L)).isEqualTo(8);

        seeder.run();
        assertThat(ofertaRepository.countByPuestoId(2L)).isEqualTo(8);

        for (Oferta oferta : ofertaRepository.findByPuestoId(2L).subList(3, 8)) {
            ofertaRepository.deleteById(oferta.getId());
        }
        assertThat(ofertaRepository.countByPuestoId(2L)).isEqualTo(3);

        seeder.run();
        assertThat(ofertaRepository.countByPuestoId(2L)).isEqualTo(8);
        assertThat(ofertaRepository.countByPuestoId(3L)).isEqualTo(3);
    }
}
