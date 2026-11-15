package br.com.fiap.bairron.dto;

public record BairroResponse(
        Long id,
        String nome,
        String cidade,
        String uf
) {
}
