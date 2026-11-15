package br.com.fiap.bairron.repository;

import br.com.fiap.bairron.entities.Confirmacao;
import br.com.fiap.bairron.entities.SituacaoConfirmacao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ConfirmacaoRepository extends JpaRepository<Confirmacao, Long> {
    Optional<Confirmacao> findByOcorrenciaIdAndMoradorId(Long ocorrenciaId, Long moradorId);

    Page<Confirmacao> findByOcorrenciaId(Long ocorrenciaId, Pageable pageable);

    long countByOcorrenciaIdAndSituacao(Long ocorrenciaId, SituacaoConfirmacao situacao);

    void deleteAllByOcorrenciaId(Long ocorrenciaId);
}
