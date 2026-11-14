package br.com.fiap.bairron.repository;

import br.com.fiap.bairron.entities.Ocorrencia;
import br.com.fiap.bairron.entities.Prioridade;
import br.com.fiap.bairron.entities.StatusOcorrencia;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public final class OcorrenciaSpecification {

    private OcorrenciaSpecification() {
    }

    public static Specification<Ocorrencia> filtrar(Long bairroId, Long categoriaId, StatusOcorrencia status,
                                                    Prioridade prioridade, String texto) {
        return (root, query, builder) -> {
            List<Predicate> condicoes = new ArrayList<>();
            if (bairroId != null) {
                condicoes.add(builder.equal(root.get("bairro").get("id"), bairroId));
            }
            if (categoriaId != null) {
                condicoes.add(builder.equal(root.get("categoria").get("id"), categoriaId));
            }
            if (status != null) {
                condicoes.add(builder.equal(root.get("status"), status));
            }
            if (prioridade != null) {
                condicoes.add(builder.equal(root.get("prioridade"), prioridade));
            }
            if (texto != null && !texto.isBlank()) {
                String trecho = "%" + texto.trim().toLowerCase() + "%";
                condicoes.add(builder.or(
                        builder.like(builder.lower(root.get("titulo")), trecho),
                        builder.like(builder.lower(root.get("descricao")), trecho)
                ));
            }
            return builder.and(condicoes.toArray(new Predicate[0]));
        };
    }
}
