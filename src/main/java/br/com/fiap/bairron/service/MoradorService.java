package br.com.fiap.bairron.service;

import br.com.fiap.bairron.dto.MoradorResponse;
import br.com.fiap.bairron.entities.Morador;
import br.com.fiap.bairron.exception.MoradorNaoEncontradoException;
import br.com.fiap.bairron.repository.MoradorRepository;
import br.com.fiap.bairron.security.UsuarioLogado;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
public class MoradorService {

    private static final Set<String> ORDENAVEIS = Set.of("id", "nome", "email", "papel");

    private final MoradorRepository repository;

    public MoradorService(MoradorRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public Page<MoradorResponse> listar(Pageable pageable) {
        OrdenacaoValida.verificar(pageable, ORDENAVEIS);
        return repository.findAll(pageable).map(MoradorService::resposta);
    }

    @Transactional(readOnly = true)
    public MoradorResponse buscar(Long id) {
        return resposta(buscarEntidade(id));
    }

    @Transactional(readOnly = true)
    public MoradorResponse eu(UsuarioLogado usuario) {
        return resposta(buscarEntidade(usuario.id()));
    }

    private Morador buscarEntidade(Long id) {
        return repository.findById(id).orElseThrow(() -> new MoradorNaoEncontradoException(id));
    }

    public static MoradorResponse resposta(Morador morador) {
        return new MoradorResponse(
                morador.getId(),
                morador.getNome(),
                morador.getEmail(),
                morador.getPapel(),
                morador.getBairro().getId(),
                morador.getBairro().getNome()
        );
    }
}
