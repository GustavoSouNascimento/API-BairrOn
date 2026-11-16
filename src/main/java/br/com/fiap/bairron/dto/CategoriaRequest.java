package br.com.fiap.bairron.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

public record CategoriaRequest(
        @NotBlank @Size(max = 80)
        @Schema(example = "Buraco na via") String nome,
        @NotBlank @Size(max = 300)
        @Schema(example = "Buracos em ruas e avenidas que oferecem risco ao trânsito") String descricao,
        @NotNull @Min(1) @Max(5)
        @Schema(example = "3", description = "gravidade de 1 (leve) a 5 (muito grave); entra no cálculo da prioridade") Integer gravidade
) {
}
