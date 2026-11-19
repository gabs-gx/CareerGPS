package com.careergps.demand.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import com.careergps.demand.domain.model.Oferta;
import com.careergps.demand.domain.repository.OfertaRepository;

public interface JpaOfertaRepository extends JpaRepository<Oferta, Long>, OfertaRepository {
}
