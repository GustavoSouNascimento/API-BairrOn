package br.com.fiap.bairron.service;

import br.com.fiap.bairron.dto.ConfirmacaoRequest;
import br.com.fiap.bairron.dto.ConfirmacaoResponse;
import br.com.fiap.bairron.entities.Confirmacao;
import br.com.fiap.bairron.entities.Morador;
import br.com.fiap.bairron.entities.Ocorrencia;
import br.com.fiap.bairron.entities.SituacaoConfirmacao;
import br.com.fiap.bairron.exception.MoradorNaoEncontradoException;
import br.com.fiap.bairron.repository.ConfirmacaoRepository;
import br.com.fiap.bairron.repository.MoradorRepository;
import br.com.fiap.bairron.security.UsuarioLogado;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

@Service
public class ConfirmacaoService {
    private static final Set<String> ORDENAVEIS = Set.of("id", "confirmadaEm", "situacao");

    private final ConfirmacaoRepository repository;
    private final MoradorRepository moradorRepository;
    private final OcorrenciaService ocorrenciaService;
    private final CalculadoraPrioridade calculadora;

    public ConfirmacaoService(ConfirmacaoRepository repository, MoradorRepository moradorRepository,
                              OcorrenciaService ocorrenciaService, CalculadoraPrioridade calculadora) {
        this.repository = repository;
        this.moradorRepository = moradorRepository;
        this.ocorrenciaService = ocorrenciaService;
        this.calculadora = calculadora;
    }

    @Transactional(readOnly = true)
    public Page<ConfirmacaoResponse> listar(Long ocorrenciaId, Pageable pageable) {
        ocorrenciaService.buscarEntidade(ocorrenciaId);
        OrdenacaoValida.verificar(pageable, ORDENAVEIS);
        return repository.findByOcorrenciaId(ocorrenciaId, pageable).map(this::resposta);
    }

    @Transactional
    public Resultado registrar(Long ocorrenciaId, ConfirmacaoRequest dados, UsuarioLogado usuario) {
        Ocorrencia ocorrencia = ocorrenciaService.buscarEntidade(ocorrenciaId);
        Morador morador = moradorRepository.findById(usuario.id())
                .orElseThrow(() -> new MoradorNaoEncontradoException(usuario.id()));

        Optional<Confirmacao> existente = repository.findByOcorrenciaIdAndMoradorId(ocorrenciaId, usuario.id());
        boolean criada = existente.isEmpty();
        Confirmacao confirmacao = existente.orElseGet(Confirmacao::new);
        confirmacao.setOcorrencia(ocorrencia);
        confirmacao.setMorador(morador);
        confirmacao.setSituacao(dados.situacao());
        confirmacao.setConfirmadaEm(LocalDateTime.now());
        Confirmacao salva = repository.saveAndFlush(confirmacao);

        ocorrencia.setTotalAindaExiste((int) repository.countByOcorrenciaIdAndSituacao(ocorrenciaId, SituacaoConfirmacao.AINDA_EXISTE));
        ocorrencia.setTotalResolvido((int) repository.countByOcorrenciaIdAndSituacao(ocorrenciaId, SituacaoConfirmacao.RESOLVIDO));
        calculadora.aplicar(ocorrencia);

        return new Resultado(resposta(salva), criada);
    }

    private ConfirmacaoResponse resposta(Confirmacao confirmacao) {
        Ocorrencia ocorrencia = confirmacao.getOcorrencia();
        return new ConfirmacaoResponse(
                confirmacao.getId(),
                ocorrencia.getId(),
                confirmacao.getMorador().getId(),
                confirmacao.getSituacao(),
                confirmacao.getConfirmadaEm(),
                ocorrencia.getStatus(),
                ocorrencia.getPrioridade(),
                ocorrencia.getPontuacao()
        );
    }

    public record Resultado(ConfirmacaoResponse confirmacao, boolean criada) {
    }
}
