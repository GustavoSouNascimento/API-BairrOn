package br.com.fiap.bairron.repository;

import br.com.fiap.bairron.entities.Bairro;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BairroRepository {
    Page<Bairro> findByNomeContainingIgnoreCase(String nome, Pageable pageable);

    boolean existsByNomeIgnoreCaseAndCidadeIgnoreCase(String nome, String cidade);

    boolean existsByNomeIgnoreCaseAndCidadeIgnoreCaseAndIdNot(String nome, String cidade, Long id);
}
