package br.com.fiap.bairron.dto;

import br.com.fiap.bairron.entities.Papel;

import java.time.Instant;

public record TokenResponse(
        String token,
        String tipo,
        Instant expiraEm,
        Papel papel
) {
}
