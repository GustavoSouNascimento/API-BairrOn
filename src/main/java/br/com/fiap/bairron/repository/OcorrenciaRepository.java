package br.com.fiap.bairron.repository;

import br.com.fiap.bairron.entities.Ocorrencia;
import br.com.fiap.bairron.entities.StatusOcorrencia;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OcorrenciaRepository {
    boolean existsByBairroId(Long bairroId);

    boolean existsByCategoriaId(Long categoriaId);

    List<Ocorrencia> findByCategoriaId(Long categoriaId);

    long countByBairroIdAndStatus(Long bairroId, StatusOcorrencia status);

    @Query("""
            select o from Ocorrencia o
            join fetch o.categoria
            join fetch o.bairro
            join fetch o.morador
            where o.bairro.id = :bairroId and o.status = :status
            order by o.pontuacao desc, o.criadaEm asc
            """)
    List<Ocorrencia> buscarPrioritarias(@Param("bairroId") Long bairroId, @Param("status") StatusOcorrencia status, Pageable pageable);

    @Query("""
            select o.prioridade as prioridade, count(o) as total
            from Ocorrencia o
            where o.bairro.id = :bairroId and o.status = :status
            group by o.prioridade
            """)
    List<ResumoPrioridade> resumirPorPrioridade(@Param("bairroId") Long bairroId, @Param("status") StatusOcorrencia status);
}
