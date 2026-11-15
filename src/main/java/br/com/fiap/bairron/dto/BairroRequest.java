package br.com.fiap.bairron.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record BairroRequest(
        @NotBlank @Size(max = 150)
        String nome,
        @NotBlank @Size(max = 100)
        String cidade,
        @NotBlank @Pattern(regexp = "[A-Za-z]{2}", message = "deve ter duas letras")
        String uf
) {
}
