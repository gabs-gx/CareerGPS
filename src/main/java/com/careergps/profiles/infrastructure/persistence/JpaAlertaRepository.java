package com.careergps.profiles.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import com.careergps.profiles.domain.model.Alerta;
import com.careergps.profiles.domain.repository.AlertaRepository;

public interface JpaAlertaRepository extends JpaRepository<Alerta, Long>, AlertaRepository {
}
