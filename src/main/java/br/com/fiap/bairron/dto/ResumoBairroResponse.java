package br.com.fiap.bairron.dto;

import br.com.fiap.bairron.entities.Prioridade;

import java.util.Map;

public record ResumoBairroResponse(
        Long bairroId,
        String bairroNome,
        long totalAtivas,
        long totalResolvidas,
        Map<Prioridade, Long> porPrioridade
) {
}
