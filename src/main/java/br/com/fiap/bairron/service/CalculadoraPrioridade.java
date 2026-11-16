package br.com.fiap.bairron.service;

import br.com.fiap.bairron.entities.Ocorrencia;
import br.com.fiap.bairron.entities.Prioridade;
import br.com.fiap.bairron.entities.StatusOcorrencia;
import org.springframework.stereotype.Component;

@Component
public class CalculadoraPrioridade {
    static final int PESO_GRAVIDADE = 10;
    static final int PESO_CONFIRMACAO = 5;
    static final int LIMITE_CONFIRMACOES = 10;
    static final int MINIMO_PARA_RESOLVER = 3;

    public Resultado calcular(int gravidade, long aindaExiste, long resolvido) {
        if (definirStatus(aindaExiste, resolvido) == StatusOcorrencia.RESOLVIDA) {
            return new Resultado(0, Prioridade.BAIXA);
        }
        long existe = Math.min(aindaExiste, LIMITE_CONFIRMACOES);
        long resolvidas = Math.min(resolvido, LIMITE_CONFIRMACOES);
        int pontos = (int) Math.max(0, gravidade * PESO_GRAVIDADE + existe * PESO_CONFIRMACAO - resolvidas * PESO_CONFIRMACAO);
        return new Resultado(pontos, classificar(pontos));
    }

    public StatusOcorrencia definirStatus(long aindaExiste, long resolvido) {
        return resolvido >= MINIMO_PARA_RESOLVER && resolvido > aindaExiste
                ? StatusOcorrencia.RESOLVIDA
                : StatusOcorrencia.ATIVA;
    }

    public Prioridade classificar(int pontos) {
        if (pontos >= 80) {
            return Prioridade.CRITICA;
        }
        if (pontos >= 60) {
            return Prioridade.ALTA;
        }
        if (pontos >= 40) {
            return Prioridade.MEDIA;
        }
        return Prioridade.BAIXA;
    }

    public void aplicar(Ocorrencia ocorrencia) {
        long aindaExiste = ocorrencia.getTotalAindaExiste();
        long resolvido = ocorrencia.getTotalResolvido();
        Resultado resultado = calcular(ocorrencia.getCategoria().getGravidade(), aindaExiste, resolvido);
        ocorrencia.setStatus(definirStatus(aindaExiste, resolvido));
        ocorrencia.setPontuacao(resultado.pontuacao());
        ocorrencia.setPrioridade(resultado.prioridade());
    }

    public record Resultado(int pontuacao, Prioridade prioridade) {
    }
}
