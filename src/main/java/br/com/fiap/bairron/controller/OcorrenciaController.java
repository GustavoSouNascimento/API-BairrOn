package br.com.fiap.bairron.controller;

import br.com.fiap.bairron.assembler.OcorrenciaModelAssembler;
import br.com.fiap.bairron.dto.OcorrenciaRequest;
import br.com.fiap.bairron.dto.OcorrenciaResponse;
import br.com.fiap.bairron.entities.Prioridade;
import br.com.fiap.bairron.entities.StatusOcorrencia;
import br.com.fiap.bairron.security.UsuarioLogado;
import br.com.fiap.bairron.service.OcorrenciaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ocorrencias")
@Tag(name = "ocorrências", description = "problemas do bairro registrados pelos moradores, com prioridade calculada")
public class OcorrenciaController {

    private final OcorrenciaService service;
    private final OcorrenciaModelAssembler assembler;

    public OcorrenciaController(OcorrenciaService service, OcorrenciaModelAssembler assembler) {
        this.service = service;
        this.assembler = assembler;
    }

    @GetMapping
    @Operation(summary = "listar ocorrências com filtros opcionais, paginação e ordenação",
            description = "ordene por id, titulo, pontuacao, status ou criadaEm. O padrão é pontuacao decrescente (mais prioritárias primeiro)")
    @ApiResponse(responseCode = "200", description = "página de ocorrências; pode estar vazia")
    @ApiResponse(responseCode = "400", description = "dados ou parâmetros inválidos", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    public PagedModel<EntityModel<OcorrenciaResponse>> listar(
            @Parameter(description = "id do bairro") @RequestParam(required = false) Long bairroId,
            @Parameter(description = "id da categoria") @RequestParam(required = false) Long categoriaId,
            @Parameter(description = "status: ATIVA ou RESOLVIDA") @RequestParam(required = false) StatusOcorrencia status,
            @Parameter(description = "prioridade: BAIXA, MEDIA, ALTA ou CRITICA") @RequestParam(required = false) Prioridade prioridade,
            @Parameter(description = "trecho do título ou da descrição, sem diferenciar maiúsculas e minúsculas")
            @RequestParam(required = false) String texto,
            @ParameterObject @PageableDefault(size = 10, sort = "pontuacao", direction = Sort.Direction.DESC) Pageable pageable,
            @Parameter(hidden = true) PagedResourcesAssembler<OcorrenciaResponse> pagedAssembler,
            @AuthenticationPrincipal Jwt jwt) {
        UsuarioLogado usuario = UsuarioLogado.de(jwt);
        return pagedAssembler.toModel(
                service.listar(bairroId, categoriaId, status, prioridade, texto, pageable),
                ocorrencia -> assembler.toModel(ocorrencia, usuario));
    }

    @GetMapping("/{id}")
    @Operation(summary = "consultar ocorrência por id")
    @ApiResponse(responseCode = "200", description = "ocorrência encontrada", content = @Content(mediaType = "application/json", schema = @Schema(implementation = OcorrenciaResponse.class)))
    @ApiResponse(responseCode = "404", description = "ocorrência não encontrada", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    public EntityModel<OcorrenciaResponse> buscar(
            @Parameter(description = "id da ocorrência") @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt) {
        return assembler.toModel(service.buscar(id), UsuarioLogado.de(jwt));
    }

    @PostMapping
    @Operation(summary = "registrar ocorrência; a prioridade inicial vem da gravidade da categoria")
    @ApiResponse(responseCode = "201", description = "ocorrência criada; endereço no cabeçalho location", content = @Content(mediaType = "application/json", schema = @Schema(implementation = OcorrenciaResponse.class)))
    @ApiResponse(responseCode = "400", description = "dados ou parâmetros inválidos", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "404", description = "categoria ou bairro não encontrado", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    public ResponseEntity<EntityModel<OcorrenciaResponse>> cadastrar(
            @Valid @RequestBody OcorrenciaRequest dados,
            @AuthenticationPrincipal Jwt jwt) {
        UsuarioLogado usuario = UsuarioLogado.de(jwt);
        EntityModel<OcorrenciaResponse> modelo = assembler.toModel(service.cadastrar(dados, usuario), usuario);
        return ResponseEntity.created(modelo.getRequiredLink("self").toUri()).body(modelo);
    }

    @PutMapping("/{id}")
    @Operation(summary = "atualizar todos os campos da ocorrência (autor, MODERADOR ou ADMIN)")
    @ApiResponse(responseCode = "200", description = "ocorrência atualizada", content = @Content(mediaType = "application/json", schema = @Schema(implementation = OcorrenciaResponse.class)))
    @ApiResponse(responseCode = "400", description = "dados ou parâmetros inválidos", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "403", description = "somente o autor ou um moderador pode alterar", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "404", description = "ocorrência, categoria ou bairro não encontrado", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    public EntityModel<OcorrenciaResponse> atualizar(
            @Parameter(description = "id da ocorrência") @PathVariable Long id,
            @Valid @RequestBody OcorrenciaRequest dados,
            @AuthenticationPrincipal Jwt jwt) {
        UsuarioLogado usuario = UsuarioLogado.de(jwt);
        return assembler.toModel(service.atualizar(id, dados, usuario), usuario);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "excluir ocorrência e suas confirmações (autor, MODERADOR ou ADMIN)")
    @ApiResponse(responseCode = "204", description = "ocorrência e confirmações excluídas", content = @Content)
    @ApiResponse(responseCode = "403", description = "somente o autor ou um moderador pode excluir", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "404", description = "ocorrência não encontrada", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    public ResponseEntity<Void> excluir(
            @Parameter(description = "id da ocorrência") @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt) {
        service.excluir(id, UsuarioLogado.de(jwt));
        return ResponseEntity.noContent().build();
    }
}
