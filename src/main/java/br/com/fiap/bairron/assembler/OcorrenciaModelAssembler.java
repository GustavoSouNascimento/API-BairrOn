package br.com.fiap.bairron.assembler;

import br.com.fiap.bairron.controller.BairroController;
import br.com.fiap.bairron.controller.CategoriaController;
import br.com.fiap.bairron.controller.ConfirmacaoController;
import br.com.fiap.bairron.controller.OcorrenciaController;
import br.com.fiap.bairron.dto.OcorrenciaResponse;
import br.com.fiap.bairron.security.UsuarioLogado;
import org.springframework.hateoas.EntityModel;
import org.springframework.stereotype.Component;

//--- Import static para os métodos da classe WebMvcLinkBuilder
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class OcorrenciaModelAssembler {

    public EntityModel<OcorrenciaResponse> toModel(OcorrenciaResponse ocorrencia, UsuarioLogado usuario) {
        Long id = ocorrencia.id();
        EntityModel<OcorrenciaResponse> modelo = EntityModel.of(ocorrencia,
                linkTo(methodOn(OcorrenciaController.class).buscar(id, null)).withSelfRel(),
                linkTo(methodOn(OcorrenciaController.class).listar(null, null, null, null, null, null, null, null))
                        .withRel("ocorrencias"),
                linkTo(methodOn(BairroController.class).buscar(ocorrencia.bairroId())).withRel("bairro"),
                linkTo(methodOn(CategoriaController.class).buscar(ocorrencia.categoriaId())).withRel("categoria"),
                linkTo(methodOn(ConfirmacaoController.class).listar(id, null, null)).withRel("confirmacoes"),
                linkTo(methodOn(ConfirmacaoController.class).registrar(id, null, null)).withRel("confirmar")
                        .withTitle("POST: confirmar se o problema ainda existe ou já foi resolvido"));
        if (usuario != null && usuario.podeAlterar(ocorrencia.autorId())) {
            modelo.add(
                    linkTo(methodOn(OcorrenciaController.class).atualizar(id, null, null)).withRel("atualizar")
                            .withTitle("PUT: atualizar a ocorrência"),
                    linkTo(methodOn(OcorrenciaController.class).excluir(id, null)).withRel("excluir")
                            .withTitle("DELETE: excluir a ocorrência"));
        }
        return modelo;
    }
}
