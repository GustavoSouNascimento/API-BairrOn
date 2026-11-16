package br.com.fiap.bairron.controller;

import br.com.fiap.bairron.assembler.OcorrenciaModelAssembler;
import br.com.fiap.bairron.dto.OcorrenciaResponse;
import br.com.fiap.bairron.dto.ResumoBairroResponse;
import br.com.fiap.bairron.security.UsuarioLogado;
import br.com.fiap.bairron.service.PainelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ProblemDetail;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/bairros/{bairroId}")
@Tag(name = "painel do bairro", description = "consultas de leitura frequente, mantidas em cache")
public class PainelController {

    private final PainelService service;
    private final OcorrenciaModelAssembler assembler;

    public PainelController(PainelService service, OcorrenciaModelAssembler assembler) {
        this.service = service;
        this.assembler = assembler;
    }

    @GetMapping("/resumo")
    @Operation(summary = "resumo do bairro: ocorrências ativas por prioridade e total de resolvidas")
    @ApiResponse(responseCode = "200", description = "resumo do bairro, com links para o bairro e as ocorrências prioritárias", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResumoBairroResponse.class)))
    @ApiResponse(responseCode = "404", description = "bairro não encontrado", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    public EntityModel<ResumoBairroResponse> resumo(@Parameter(description = "id do bairro") @PathVariable Long bairroId) {
        return EntityModel.of(service.resumo(bairroId),
                linkTo(methodOn(PainelController.class).resumo(bairroId)).withSelfRel(),
                linkTo(methodOn(BairroController.class).buscar(bairroId)).withRel("bairro"),
                linkTo(methodOn(PainelController.class).prioritarias(bairroId, 5, null)).withRel("prioritarias"));
    }

    @GetMapping("/ocorrencias/prioritarias")
    @Operation(summary = "ocorrências ativas do bairro, da mais para a menos prioritária")
    @ApiResponse(responseCode = "200", description = "lista ordenada por pontuação")
    @ApiResponse(responseCode = "400", description = "limite fora do intervalo de 1 a 20", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "404", description = "bairro não encontrado", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    public CollectionModel<EntityModel<OcorrenciaResponse>> prioritarias(
            @Parameter(description = "id do bairro") @PathVariable Long bairroId,
            @Parameter(description = "quantidade máxima de ocorrências, de 1 a 20") @RequestParam(defaultValue = "5") int limite,
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
