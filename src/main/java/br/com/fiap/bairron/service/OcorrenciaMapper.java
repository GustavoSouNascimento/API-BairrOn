package br.com.fiap.bairron.service;

import br.com.fiap.bairron.dto.OcorrenciaResponse;
import br.com.fiap.bairron.entities.Ocorrencia;

public final class OcorrenciaMapper {
    private OcorrenciaMapper() {
    }

    public static OcorrenciaResponse resposta(Ocorrencia ocorrencia) {
        return new OcorrenciaResponse(
                ocorrencia.getId(),
                ocorrencia.getTitulo(),
                ocorrencia.getDescricao(),
                ocorrencia.getLatitude(),
                ocorrencia.getLongitude(),
                ocorrencia.getStatus(),
                ocorrencia.getPrioridade(),
                ocorrencia.getPontuacao(),
                ocorrencia.getTotalAindaExiste(),
                ocorrencia.getTotalResolvido(),
                ocorrencia.getCriadaEm(),
                ocorrencia.getCategoria().getId(),
                ocorrencia.getCategoria().getNome(),
                ocorrencia.getBairro().getId(),
                ocorrencia.getBairro().getNome(),
                ocorrencia.getMorador().getId()
        );
    }
}
