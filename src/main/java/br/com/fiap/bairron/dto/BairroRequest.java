package br.com.fiap.bairron.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record BairroRequest(
        @NotBlank @Size(max = 150)
        @Schema(example = "Mooca") String nome,
        @NotBlank @Size(max = 100)
        @Schema(example = "São Paulo") String cidade,
        @NotBlank @Pattern(regexp = "[A-Za-z]{2}", message = "deve ter duas letras")
        @Schema(example = "SP") String uf
) {
}
