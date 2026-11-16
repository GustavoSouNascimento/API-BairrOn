package br.com.fiap.bairron.service;

import br.com.fiap.bairron.config.CachesPainel;
import br.com.fiap.bairron.dto.BairroRequest;
import br.com.fiap.bairron.dto.BairroResponse;
import br.com.fiap.bairron.entities.Bairro;
import br.com.fiap.bairron.exception.BairroNaoEncontradoException;
import br.com.fiap.bairron.exception.ConflitoException;
import br.com.fiap.bairron.repository.BairroRepository;
import br.com.fiap.bairron.repository.MoradorRepository;
import br.com.fiap.bairron.repository.OcorrenciaRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
public class BairroService {
    private static final Set<String> ORDENAVEIS = Set.of("id", "nome", "cidade", "uf");

    private final BairroRepository repository;
    private final MoradorRepository moradorRepository;
    private final OcorrenciaRepository ocorrenciaRepository;

    public BairroService(BairroRepository repository, MoradorRepository moradorRepository,
                         OcorrenciaRepository ocorrenciaRepository) {
        this.repository = repository;
        this.moradorRepository = moradorRepository;
        this.ocorrenciaRepository = ocorrenciaRepository;
    }

    @Transactional(readOnly = true)
    public Page<BairroResponse> listar(String nome, Pageable pageable) {
        OrdenacaoValida.verificar(pageable, ORDENAVEIS);
        Page<Bairro> bairros = nome == null || nome.isBlank()
                ? repository.findAll(pageable)
                : repository.findByNomeContainingIgnoreCase(nome.trim(), pageable);
        return bairros.map(this::resposta);
    }

    @Transactional(readOnly = true)
    public BairroResponse buscar(Long id) {
        return resposta(buscarEntidade(id));
    }

    @Transactional
    public BairroResponse cadastrar(BairroRequest dados) {
        String nome = dados.nome().trim();
        String cidade = dados.cidade().trim();
        if (repository.existsByNomeIgnoreCaseAndCidadeIgnoreCase(nome, cidade)) {
            throw new ConflitoException("Já existe um bairro com este nome nesta cidade.");
        }
        return resposta(repository.save(preencher(new Bairro(), dados)));
    }

    @Transactional
    @CacheEvict(cacheNames = {CachesPainel.RESUMO, CachesPainel.PRIORITARIAS}, allEntries = true)
    public BairroResponse atualizar(Long id, BairroRequest dados) {
        Bairro bairro = buscarEntidade(id);
        if (repository.existsByNomeIgnoreCaseAndCidadeIgnoreCaseAndIdNot(dados.nome().trim(), dados.cidade().trim(), id)) {
            throw new ConflitoException("Já existe um bairro com este nome nesta cidade.");
        }
        return resposta(repository.save(preencher(bairro, dados)));
    }

    @Transactional
    @CacheEvict(cacheNames = {CachesPainel.RESUMO, CachesPainel.PRIORITARIAS}, allEntries = true)
    public void excluir(Long id) {
        Bairro bairro = buscarEntidade(id);
        if (moradorRepository.existsByBairroId(id) || ocorrenciaRepository.existsByBairroId(id)) {
            throw new ConflitoException("O bairro possui moradores ou ocorrências vinculados e não pode ser excluído.");
        }
        repository.delete(bairro);
    }

    Bairro buscarEntidade(Long id) {
        return repository.findById(id).orElseThrow(() -> new BairroNaoEncontradoException(id));
    }

    private Bairro preencher(Bairro bairro, BairroRequest dados) {
        bairro.setNome(dados.nome().trim());
        bairro.setCidade(dados.cidade().trim());
        bairro.setUf(dados.uf().trim().toUpperCase());
        return bairro;
    }

    private BairroResponse resposta(Bairro bairro) {
        return new BairroResponse(bairro.getId(), bairro.getNome(), bairro.getCidade(), bairro.getUf());
    }
}
