package br.com.fiap.bairron.dto;

import br.com.fiap.bairron.entities.SituacaoConfirmacao;
import jakarta.validation.constraints.NotNull;

public record ConfirmacaoRequest(
        @NotNull
        SituacaoConfirmacao situacao
) {
}
