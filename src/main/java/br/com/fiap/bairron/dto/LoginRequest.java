package br.com.fiap.bairron.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank @Email
        @Schema(example = "maria@bairron.com") String email,
        @NotBlank
        @Schema(example = "Senha@123") String senha
) {
}
