package br.com.fiap.bairron.controller;

import br.com.fiap.bairron.dto.ConfirmacaoRequest;
import br.com.fiap.bairron.dto.ConfirmacaoResponse;
import br.com.fiap.bairron.security.UsuarioLogado;
import br.com.fiap.bairron.service.ConfirmacaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/ocorrencias/{ocorrenciaId}/confirmacoes")
@Tag(name = "confirmações", description = "moradores confirmam se o problema ainda existe ou já foi resolvido")
public class ConfirmacaoController {

    private final ConfirmacaoService service;

    public ConfirmacaoController(ConfirmacaoService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "listar confirmações de uma ocorrência com paginação e ordenação")
    @ApiResponse(responseCode = "200", description = "página de confirmações; pode estar vazia")
    @ApiResponse(responseCode = "400", description = "dados ou parâmetros inválidos", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "404", description = "ocorrência não encontrada", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    public PagedModel<EntityModel<ConfirmacaoResponse>> listar(
            @Parameter(description = "id da ocorrência") @PathVariable Long ocorrenciaId,
            @ParameterObject @PageableDefault(size = 10, sort = "confirmadaEm", direction = Sort.Direction.DESC) Pageable pageable,
            @Parameter(hidden = true) PagedResourcesAssembler<ConfirmacaoResponse> pagedAssembler) {
        return pagedAssembler.toModel(service.listar(ocorrenciaId, pageable), this::modelo);
    }

    @PostMapping
    @Operation(summary = "confirmar a situação da ocorrência",
            description = "cada morador tem uma confirmação por ocorrência: a primeira devolve 201 e as seguintes trocam a situação e devolvem 200. "
                    + "A resposta já traz o status e a prioridade recalculados")
    @ApiResponse(responseCode = "201", description = "confirmação criada", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ConfirmacaoResponse.class)))
    @ApiResponse(responseCode = "200", description = "confirmação anterior do morador atualizada", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ConfirmacaoResponse.class)))
    @ApiResponse(responseCode = "400", description = "dados ou parâmetros inválidos", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "404", description = "ocorrência não encontrada", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    public ResponseEntity<EntityModel<ConfirmacaoResponse>> registrar(
            @Parameter(description = "id da ocorrência") @PathVariable Long ocorrenciaId,
            @Valid @RequestBody ConfirmacaoRequest dados,
            @AuthenticationPrincipal Jwt jwt) {
        ConfirmacaoService.Resultado resultado = service.registrar(ocorrenciaId, dados, UsuarioLogado.de(jwt));
        HttpStatus status = resultado.criada() ? HttpStatus.CREATED : HttpStatus.OK;
        return ResponseEntity.status(status).body(modelo(resultado.confirmacao()));
    }

    private EntityModel<ConfirmacaoResponse> modelo(ConfirmacaoResponse confirmacao) {
        return EntityModel.of(confirmacao,
                linkTo(methodOn(OcorrenciaController.class).buscar(confirmacao.ocorrenciaId(), null)).withRel("ocorrencia"),
                linkTo(methodOn(ConfirmacaoController.class).listar(confirmacao.ocorrenciaId(), null, null))
                        .withRel("confirmacoes"));
    }
}
