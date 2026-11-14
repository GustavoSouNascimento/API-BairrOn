package br.com.fiap.bairron.repository;

import br.com.fiap.bairron.entities.Morador;

import java.util.Optional;

public interface MoradorRepository {
    Optional<Morador> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByBairroId(Long bairroId);
}
