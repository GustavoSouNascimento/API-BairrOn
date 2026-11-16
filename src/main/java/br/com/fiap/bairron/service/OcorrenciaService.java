package br.com.fiap.bairron.service;

import br.com.fiap.bairron.config.CachesPainel;
import br.com.fiap.bairron.dto.OcorrenciaRequest;
import br.com.fiap.bairron.dto.OcorrenciaResponse;
import br.com.fiap.bairron.entities.Ocorrencia;
import br.com.fiap.bairron.entities.Prioridade;
import br.com.fiap.bairron.entities.StatusOcorrencia;
import br.com.fiap.bairron.exception.AcessoNegadoException;
import br.com.fiap.bairron.exception.MoradorNaoEncontradoException;
import br.com.fiap.bairron.exception.OcorrenciaNaoEncontradaException;
import br.com.fiap.bairron.repository.ConfirmacaoRepository;
import br.com.fiap.bairron.repository.MoradorRepository;
import br.com.fiap.bairron.repository.OcorrenciaRepository;
import br.com.fiap.bairron.repository.OcorrenciaSpecification;
import br.com.fiap.bairron.security.UsuarioLogado;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Set;

@Service
public class OcorrenciaService {
    private static final Set<String> ORDENAVEIS = Set.of("id", "titulo", "pontuacao", "status", "criadaEm");

    private final OcorrenciaRepository repository;
    private final ConfirmacaoRepository confirmacaoRepository;
    private final MoradorRepository moradorRepository;
    private final BairroService bairroService;
    private final CategoriaService categoriaService;
    private final CalculadoraPrioridade calculadora;

    public OcorrenciaService(OcorrenciaRepository repository, ConfirmacaoRepository confirmacaoRepository,
                             MoradorRepository moradorRepository, BairroService bairroService,
                             CategoriaService categoriaService, CalculadoraPrioridade calculadora) {
        this.repository = repository;
        this.confirmacaoRepository = confirmacaoRepository;
        this.moradorRepository = moradorRepository;
        this.bairroService = bairroService;
        this.categoriaService = categoriaService;
        this.calculadora = calculadora;
    }

    @Transactional(readOnly = true)
    public Page<OcorrenciaResponse> listar(Long bairroId, Long categoriaId, StatusOcorrencia status,
                                           Prioridade prioridade, String texto, Pageable pageable) {
        OrdenacaoValida.verificar(pageable, ORDENAVEIS);
        return repository.findAll(OcorrenciaSpecification.filtrar(bairroId, categoriaId, status, prioridade, texto), pageable)
                .map(OcorrenciaMapper::resposta);
    }

    @Transactional(readOnly = true)
    public OcorrenciaResponse buscar(Long id) {
        return OcorrenciaMapper.resposta(buscarEntidade(id));
    }

    @Transactional
    @CacheEvict(cacheNames = {CachesPainel.RESUMO, CachesPainel.PRIORITARIAS}, allEntries = true)
    public OcorrenciaResponse cadastrar(OcorrenciaRequest dados, UsuarioLogado usuario) {
        Ocorrencia ocorrencia = preencher(new Ocorrencia(), dados);
        ocorrencia.setMorador(moradorRepository.findById(usuario.id())
                .orElseThrow(() -> new MoradorNaoEncontradoException(usuario.id())));
        ocorrencia.setCriadaEm(LocalDateTime.now());
        calculadora.aplicar(ocorrencia);
        return OcorrenciaMapper.resposta(repository.save(ocorrencia));
    }

    @Transactional
    @CacheEvict(cacheNames = {CachesPainel.RESUMO, CachesPainel.PRIORITARIAS}, allEntries = true)
    public OcorrenciaResponse atualizar(Long id, OcorrenciaRequest dados, UsuarioLogado usuario) {
        Ocorrencia ocorrencia = buscarEntidade(id);
        verificarPermissao(ocorrencia, usuario);
        preencher(ocorrencia, dados);
        calculadora.aplicar(ocorrencia);
        return OcorrenciaMapper.resposta(repository.save(ocorrencia));
    }

    @Transactional
    @CacheEvict(cacheNames = {CachesPainel.RESUMO, CachesPainel.PRIORITARIAS}, allEntries = true)
    public void excluir(Long id, UsuarioLogado usuario) {
        Ocorrencia ocorrencia = buscarEntidade(id);
        verificarPermissao(ocorrencia, usuario);
        confirmacaoRepository.deleteAllByOcorrenciaId(id);
        repository.delete(ocorrencia);
    }

    Ocorrencia buscarEntidade(Long id) {
        return repository.findById(id).orElseThrow(() -> new OcorrenciaNaoEncontradaException(id));
    }

    private void verificarPermissao(Ocorrencia ocorrencia, UsuarioLogado usuario) {
        if (!usuario.podeAlterar(ocorrencia.getMorador().getId())) {
            throw new AcessoNegadoException("Somente o autor da ocorrência ou um moderador pode alterá-la.");
        }
    }

    private Ocorrencia preencher(Ocorrencia ocorrencia, OcorrenciaRequest dados) {
        ocorrencia.setTitulo(dados.titulo().trim());
        ocorrencia.setDescricao(dados.descricao().trim());
        ocorrencia.setLatitude(dados.latitude());
        ocorrencia.setLongitude(dados.longitude());
        ocorrencia.setCategoria(categoriaService.buscarEntidade(dados.categoriaId()));
        ocorrencia.setBairro(bairroService.buscarEntidade(dados.bairroId()));
        return ocorrencia;
    }
}
