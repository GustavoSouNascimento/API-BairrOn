package br.com.fiap.bairron.dto;

import jakarta.validation.constraints.*;

public record CategoriaRequest(
        @NotBlank @Size(max = 80)
        String nome,
        @NotBlank @Size(max = 300)
        String descricao,
        @NotNull @Min(1) @Max(5)
        Integer gravidade
) {
}
