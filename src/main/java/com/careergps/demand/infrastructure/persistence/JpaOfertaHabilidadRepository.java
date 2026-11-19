package com.careergps.demand.infrastructure.persistence;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.careergps.demand.domain.model.FrecuenciaHabilidad;
import com.careergps.demand.domain.model.OfertaHabilidad;
import com.careergps.demand.domain.repository.OfertaHabilidadRepository;

public interface JpaOfertaHabilidadRepository extends JpaRepository<OfertaHabilidad, Long>, OfertaHabilidadRepository {

    @Override
    @Query("""
            select new com.careergps.demand.domain.model.FrecuenciaHabilidad(h.id, h.nombre, count(oh))
            from OfertaHabilidad oh, Habilidad h, Oferta o
            where oh.habilidadId = h.id
              and oh.ofertaId = o.id
              and o.puestoId = :puestoId
            group by h.id, h.nombre
            order by count(oh) desc
            """)
    List<FrecuenciaHabilidad> contarFrecuenciaPorPuesto(@Param("puestoId") Long puestoId);
}
