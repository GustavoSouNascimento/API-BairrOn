package br.com.fiap.bairron.dto;

import jakarta.validation.constraints.*;

public record OcorrenciaRequest(
        @NotBlank @Size(max = 150)
        String titulo,
        @NotBlank @Size(max = 1000)
        String descricao,
        @NotNull @DecimalMin("-90.0") @DecimalMax("90.0")
        Double latitude,
        @NotNull @DecimalMin("-180.0") @DecimalMax("180.0")
        Double longitude,
        @NotNull
        Long categoriaId,
        @NotNull
        Long bairroId
) {
}
