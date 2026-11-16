package br.com.fiap.bairron.dto;

import org.springframework.hateoas.server.core.Relation;

@Relation(collectionRelation = "categorias", itemRelation = "categoria")
public record CategoriaResponse(
        Long id,
        String nome,
        String descricao,
        Integer gravidade
) {
}
