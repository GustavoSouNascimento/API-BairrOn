package br.com.fiap.bairron.dto;

import org.springframework.hateoas.server.core.Relation;

@Relation(collectionRelation = "bairros", itemRelation = "bairro")
public record BairroResponse(
        Long id,
        String nome,
        String cidade,
        String uf
) {
}
