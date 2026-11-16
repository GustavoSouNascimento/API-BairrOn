package br.com.fiap.bairron.assembler;

import br.com.fiap.bairron.controller.BairroController;
import br.com.fiap.bairron.controller.PainelController;
import br.com.fiap.bairron.dto.BairroResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

//--- Import static para os métodos da classe WebMvcLinkBuilder
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class BairroModelAssembler implements RepresentationModelAssembler<BairroResponse, EntityModel<BairroResponse>> {

    @Override
    public EntityModel<BairroResponse> toModel(BairroResponse bairro) {
        return EntityModel.of(bairro,
                linkTo(methodOn(BairroController.class).buscar(bairro.id())).withSelfRel(),
                linkTo(methodOn(BairroController.class).listar(null, null, null)).withRel("bairros"),
                linkTo(methodOn(PainelController.class).resumo(bairro.id())).withRel("resumo"),
                linkTo(methodOn(PainelController.class).prioritarias(bairro.id(), 5, null)).withRel("prioritarias"));
    }
}
