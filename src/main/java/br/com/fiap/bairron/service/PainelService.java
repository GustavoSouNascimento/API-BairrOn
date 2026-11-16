package br.com.fiap.bairron.service;

import br.com.fiap.bairron.config.CachesPainel;
import br.com.fiap.bairron.dto.OcorrenciaResponse;
import br.com.fiap.bairron.dto.ResumoBairroResponse;
import br.com.fiap.bairron.entities.Bairro;
import br.com.fiap.bairron.entities.Prioridade;
import br.com.fiap.bairron.entities.StatusOcorrencia;
import br.com.fiap.bairron.exception.ParametroInvalidoException;
import br.com.fiap.bairron.repository.OcorrenciaRepository;
import br.com.fiap.bairron.repository.ResumoPrioridade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
public class PainelService {
    private static final int LIMITE_MAXIMO = 20;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final OcorrenciaRepository ocorrenciaRepository;
    private final BairroService bairroService;

    public PainelService(OcorrenciaRepository ocorrenciaRepository, BairroService bairroService) {
        this.ocorrenciaRepository = ocorrenciaRepository;
        this.bairroService = bairroService;
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = CachesPainel.RESUMO, key = "#bairroId")
    public ResumoBairroResponse resumo(Long bairroId) {
        logger.info("Consultando o resumo do bairro {} no banco ...", bairroId);
        Bairro bairro = bairroService.buscarEntidade(bairroId);
        Map<Prioridade, Long> porPrioridade = new EnumMap<>(Prioridade.class);
        for (Prioridade prioridade : Prioridade.values()) {
            porPrioridade.put(prioridade, 0L);
        }
        long ativas = 0;
        for (ResumoPrioridade linha : ocorrenciaRepository.resumirPorPrioridade(bairroId, StatusOcorrencia.ATIVA)) {
            porPrioridade.put(linha.getPrioridade(), linha.getTotal());
            ativas += linha.getTotal();
        }
        long resolvidas = ocorrenciaRepository.countByBairroIdAndStatus(bairroId, StatusOcorrencia.RESOLVIDA);
        return new ResumoBairroResponse(bairro.getId(), bairro.getNome(), ativas, resolvidas, porPrioridade);
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = CachesPainel.PRIORITARIAS, key = "#bairroId + ':' + #limite")
    public List<OcorrenciaResponse> prioritarias(Long bairroId, int limite) {
        if (limite < 1 || limite > LIMITE_MAXIMO) {
            throw new ParametroInvalidoException("O limite deve estar entre 1 e " + LIMITE_MAXIMO + ".");
        }
        bairroService.buscarEntidade(bairroId);
        logger.info("Consultando as ocorrências prioritárias do bairro {} (limite {}) no banco ...", bairroId, limite);
        return ocorrenciaRepository.buscarPrioritarias(bairroId, StatusOcorrencia.ATIVA, PageRequest.of(0, limite))
                .stream()
                .map(OcorrenciaMapper::resposta)
                .toList();
    }
}