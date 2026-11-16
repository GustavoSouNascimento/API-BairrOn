package br.com.fiap.bairron.assembler;

import br.com.fiap.bairron.controller.CategoriaController;
import br.com.fiap.bairron.dto.CategoriaResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

//--- Import static para os métodos da classe WebMvcLinkBuilder
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class CategoriaModelAssembler implements RepresentationModelAssembler<CategoriaResponse, EntityModel<CategoriaResponse>> {

    @Override
    public EntityModel<CategoriaResponse> toModel(CategoriaResponse categoria) {
        return EntityModel.of(categoria,
                linkTo(methodOn(CategoriaController.class).buscar(categoria.id())).withSelfRel(),
                linkTo(methodOn(CategoriaController.class).listar(null, null, null)).withRel("categorias"));
    }
}
