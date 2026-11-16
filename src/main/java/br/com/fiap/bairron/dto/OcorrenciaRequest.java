package br.com.fiap.bairron.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

public record OcorrenciaRequest(
        @NotBlank @Size(max = 150)
        @Schema(example = "Buraco grande na avenida principal") String titulo,
        @NotBlank @Size(max = 1000)
        @Schema(example = "O buraco ocupa metade da faixa e já causou dois furos de pneu.") String descricao,
        @NotNull @DecimalMin("-90.0") @DecimalMax("90.0")
        @Schema(example = "-23.5505") Double latitude,
        @NotNull @DecimalMin("-180.0") @DecimalMax("180.0")
        @Schema(example = "-46.6333") Double longitude,
        @NotNull
        @Schema(example = "1", description = "id da categoria do problema") Long categoriaId,
        @NotNull
        @Schema(example = "1", description = "id do bairro onde o problema acontece") Long bairroId
) {
}
