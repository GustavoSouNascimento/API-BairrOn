package br.com.fiap.bairron.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CadastroRequest(
        @NotBlank @Size(max = 120)
        @Schema(example = "Fernanda Lima") String nome,
        @NotBlank @Email @Size(max = 150)
        @Schema(example = "fernanda@exemplo.com") String email,
        @NotBlank @Size(min = 8, max = 72)
        @Schema(example = "Senha@123") String senha,
        @NotNull
        @Schema(example = "1", description = "id do bairro onde o morador vive") Long bairroId
) {
}
