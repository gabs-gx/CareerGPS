package com.careergps.profiles.application.dto;

import java.time.Instant;

public record AlertaDTO(Long id, String tipo, String texto, Instant creadaEn, boolean leida) {
}
