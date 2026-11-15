package br.com.fiap.bairron.repository;

import br.com.fiap.bairron.entities.Morador;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MoradorRepository extends JpaRepository<Morador, Long> {
    Optional<Morador> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByBairroId(Long bairroId);
}
