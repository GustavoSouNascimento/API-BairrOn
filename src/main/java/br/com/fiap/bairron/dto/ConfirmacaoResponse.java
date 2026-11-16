package br.com.fiap.bairron.dto;

import br.com.fiap.bairron.entities.Prioridade;
import br.com.fiap.bairron.entities.SituacaoConfirmacao;
import br.com.fiap.bairron.entities.StatusOcorrencia;

import java.time.LocalDateTime;

public record ConfirmacaoResponse(
        Long id,
        Long ocorrenciaId,
        Long moradorId,
        SituacaoConfirmacao situacao,
        LocalDateTime confirmadaEm,
        StatusOcorrencia statusOcorrencia,
        Prioridade prioridadeOcorrencia,
        Integer pontuacaoOcorrencia
) {
}
