package br.com.fiap.bairron.dto;

import br.com.fiap.bairron.entities.Prioridade;
import br.com.fiap.bairron.entities.StatusOcorrencia;

import java.time.LocalDateTime;

public record OcorrenciaResponse(
        Long id,
        String titulo,
        String descricao,
        Double latitude,
        Double longitude,
        StatusOcorrencia status,
        Prioridade prioridade,
        Integer pontuacao,
        Integer totalAindaExiste,
        Integer totalResolvido,
        LocalDateTime criadaEm,
        Long categoriaId,
        String categoriaNome,
        Long bairroId,
        String bairroNome,
        Long autorId
) {
}
