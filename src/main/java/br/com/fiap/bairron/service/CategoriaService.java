package br.com.fiap.bairron.service;

import br.com.fiap.bairron.dto.CategoriaRequest;
import br.com.fiap.bairron.dto.CategoriaResponse;
import br.com.fiap.bairron.entities.Categoria;
import br.com.fiap.bairron.exception.CategoriaNaoEncontradaException;
import br.com.fiap.bairron.exception.ConflitoException;
import br.com.fiap.bairron.repository.CategoriaRepository;
import br.com.fiap.bairron.repository.OcorrenciaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
public class CategoriaService {
    private static final Set<String> ORDENAVEIS = Set.of("id", "nome", "gravidade");

    private final CategoriaRepository repository;
    private final OcorrenciaRepository ocorrenciaRepository;
    private final CalculadoraPrioridade calculadora;

    public CategoriaService(CategoriaRepository repository, OcorrenciaRepository ocorrenciaRepository, CalculadoraPrioridade calculadora) {
        this.repository = repository;
        this.ocorrenciaRepository = ocorrenciaRepository;
        this.calculadora = calculadora;
    }

    @Transactional(readOnly = true)
    public Page<CategoriaResponse> listar(String nome, Pageable pageable) {
        OrdenacaoValida.verificar(pageable, ORDENAVEIS);
        Page<Categoria> categorias = nome == null || nome.isBlank()
                ? repository.findAll(pageable)
                : repository.findByNomeContainingIgnoreCase(nome.trim(), pageable);
        return categorias.map(this::resposta);
    }

    @Transactional(readOnly = true)
    public CategoriaResponse buscar(Long id) {
        return resposta(buscarEntidade(id));
    }

    @Transactional
    public CategoriaResponse cadastrar(CategoriaRequest dados) {
        if (repository.existsByNomeIgnoreCase(dados.nome().trim())) {
            throw new ConflitoException("Já existe uma categoria com este nome.");
        }
        return resposta(repository.save(preencher(new Categoria(), dados)));
    }

    @Transactional
    public CategoriaResponse atualizar(Long id, CategoriaRequest dados) {
        Categoria categoria = buscarEntidade(id);
        if (repository.existsByNomeIgnoreCaseAndIdNot(dados.nome().trim(), id)) {
            throw new ConflitoException("Já existe uma categoria com este nome.");
        }
        Categoria salva = repository.save(preencher(categoria, dados));
        ocorrenciaRepository.findByCategoriaId(id).forEach(calculadora::aplicar);
        return resposta(salva);
    }

    @Transactional
    public void excluir(Long id) {
        Categoria categoria = buscarEntidade(id);
        if (ocorrenciaRepository.existsByCategoriaId(id)) {
            throw new ConflitoException("A categoria possui ocorrências vinculadas e não pode ser excluída.");
        }
        repository.delete(categoria);
    }

    Categoria buscarEntidade(Long id) {
        return repository.findById(id).orElseThrow(() -> new CategoriaNaoEncontradaException(id));
    }

    private Categoria preencher(Categoria categoria, CategoriaRequest dados) {
        categoria.setNome(dados.nome().trim());
        categoria.setDescricao(dados.descricao().trim());
        categoria.setGravidade(dados.gravidade());
        return categoria;
    }

    private CategoriaResponse resposta(Categoria categoria) {
        return new CategoriaResponse(categoria.getId(), categoria.getNome(), categoria.getDescricao(), categoria.getGravidade());
    }
}
