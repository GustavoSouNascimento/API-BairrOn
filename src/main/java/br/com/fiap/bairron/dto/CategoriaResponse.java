package br.com.fiap.bairron.dto;

public record CategoriaResponse(
        Long id,
        String nome,
        String descricao,
        Integer gravidade
) {
}
