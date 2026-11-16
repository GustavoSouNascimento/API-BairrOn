package br.com.fiap.bairron.dto;

import br.com.fiap.bairron.entities.Papel;

public record MoradorResponse(
        Long id,
        String nome,
        String email,
        Papel papel,
        Long bairroId,
        String bairroNome
) {
}
