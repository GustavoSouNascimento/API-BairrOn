package br.com.fiap.bairron.dto;

import br.com.fiap.bairron.entities.SituacaoConfirmacao;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record ConfirmacaoRequest(
        @NotNull
        @Schema(example = "AINDA_EXISTE", description = "AINDA_EXISTE ou RESOLVIDO") SituacaoConfirmacao situacao
) {
}
