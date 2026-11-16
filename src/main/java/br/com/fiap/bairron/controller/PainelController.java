package br.com.fiap.bairron.controller;

import br.com.fiap.bairron.assembler.OcorrenciaModelAssembler;
import br.com.fiap.bairron.dto.OcorrenciaResponse;
import br.com.fiap.bairron.dto.ResumoBairroResponse;
import br.com.fiap.bairron.security.UsuarioLogado;
import br.com.fiap.bairron.service.PainelService;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/bairros/{bairroId}")
public class PainelController {
    private final PainelService service;
    private final OcorrenciaModelAssembler assembler;

    public PainelController(PainelService service, OcorrenciaModelAssembler assembler) {
        this.service = service;
        this.assembler = assembler;
    }

    @GetMapping("/resumo")
    public EntityModel<ResumoBairroResponse> resumo(@PathVariable Long bairroId) {
        return EntityModel.of(service.resumo(bairroId),
                linkTo(methodOn(PainelController.class).resumo(bairroId)).withSelfRel(),
                linkTo(methodOn(BairroController.class).buscar(bairroId)).withRel("bairro"),
                linkTo(methodOn(PainelController.class).prioritarias(bairroId, 5, null)).withRel("prioritarias"));
    }

    @GetMapping("/ocorrencias/prioritarias")
    public CollectionModel<EntityModel<OcorrenciaResponse>> prioritarias(
            @PathVariable Long bairroId,
            @RequestParam(defaultValue = "5") int limite,
            @AuthenticationPrincipal Jwt jwt) {
        UsuarioLogado usuario = UsuarioLogado.de(jwt);
        List<OcorrenciaResponse> ocorrencias = service.prioritarias(bairroId, limite);
        return CollectionModel.of(
                ocorrencias.stream().map(ocorrencia -> assembler.toModel(ocorrencia, usuario)).toList(),
                linkTo(methodOn(PainelController.class).prioritarias(bairroId, limite, null)).withSelfRel(),
                linkTo(methodOn(BairroController.class).buscar(bairroId)).withRel("bairro"),
                linkTo(methodOn(PainelController.class).resumo(bairroId)).withRel("resumo"));
    }
}
