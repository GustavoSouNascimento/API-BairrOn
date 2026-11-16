package br.com.fiap.bairron.dto;

import br.com.fiap.bairron.entities.Papel;
import org.springframework.hateoas.server.core.Relation;

@Relation(collectionRelation = "moradores", itemRelation = "morador")
public record MoradorResponse(
        Long id,
        String nome,
        String email,
        Papel papel,
        Long bairroId,
        String bairroNome
) {
}
