package br.com.fiap.bairron.controller;

import br.com.fiap.bairron.assembler.CategoriaModelAssembler;
import br.com.fiap.bairron.dto.CategoriaRequest;
import br.com.fiap.bairron.dto.CategoriaResponse;
import br.com.fiap.bairron.service.CategoriaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/categorias")
@Tag(name = "categorias", description = "categorias de problema e sua gravidade; consulta pública, escrita só para moderadores e administradores")
public class CategoriaController {

    private final CategoriaService service;
    private final CategoriaModelAssembler assembler;

    public CategoriaController(CategoriaService service, CategoriaModelAssembler assembler) {
        this.service = service;
        this.assembler = assembler;
    }

    @GetMapping
    @SecurityRequirements
    @Operation(summary = "listar categorias com paginação, ordenação e filtro opcional por nome")
    @ApiResponse(responseCode = "200", description = "página de categorias; pode estar vazia")
    @ApiResponse(responseCode = "400", description = "dados ou parâmetros inválidos", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    public PagedModel<EntityModel<CategoriaResponse>> listar(
            @Parameter(description = "trecho do nome, sem diferenciar maiúsculas e minúsculas")
            @RequestParam(required = false) String nome,
            @ParameterObject @PageableDefault(size = 10, sort = "nome") Pageable pageable,
            @Parameter(hidden = true) PagedResourcesAssembler<CategoriaResponse> pagedAssembler) {
        return pagedAssembler.toModel(service.listar(nome, pageable), assembler);
    }

    @GetMapping("/{id}")
    @SecurityRequirements
    @Operation(summary = "consultar categoria por id")
    @ApiResponse(responseCode = "200", description = "categoria encontrada", content = @Content(mediaType = "application/json", schema = @Schema(implementation = CategoriaResponse.class)))
    @ApiResponse(responseCode = "404", description = "categoria não encontrada", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    public EntityModel<CategoriaResponse> buscar(@Parameter(description = "id da categoria") @PathVariable Long id) {
        return assembler.toModel(service.buscar(id));
    }

    @PostMapping
    @Operation(summary = "cadastrar categoria (MODERADOR ou ADMIN)")
    @ApiResponse(responseCode = "201", description = "categoria criada; endereço no cabeçalho location", content = @Content(mediaType = "application/json", schema = @Schema(implementation = CategoriaResponse.class)))
    @ApiResponse(responseCode = "400", description = "dados ou parâmetros inválidos", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "403", description = "papel sem permissão", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "409", description = "já existe uma categoria com este nome", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    public ResponseEntity<EntityModel<CategoriaResponse>> cadastrar(@Valid @RequestBody CategoriaRequest dados) {
        EntityModel<CategoriaResponse> modelo = assembler.toModel(service.cadastrar(dados));
        return ResponseEntity.created(modelo.getRequiredLink("self").toUri()).body(modelo);
    }

    @PutMapping("/{id}")
    @Operation(summary = "atualizar todos os campos da categoria (MODERADOR ou ADMIN)")
    @ApiResponse(responseCode = "200", description = "categoria atualizada", content = @Content(mediaType = "application/json", schema = @Schema(implementation = CategoriaResponse.class)))
    @ApiResponse(responseCode = "400", description = "dados ou parâmetros inválidos", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "403", description = "papel sem permissão", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "404", description = "categoria não encontrada", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "409", description = "já existe uma categoria com este nome", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    public EntityModel<CategoriaResponse> atualizar(
            @Parameter(description = "id da categoria") @PathVariable Long id,
            @Valid @RequestBody CategoriaRequest dados) {
        return assembler.toModel(service.atualizar(id, dados));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "excluir categoria sem ocorrências (MODERADOR ou ADMIN)")
    @ApiResponse(responseCode = "204", description = "categoria excluída", content = @Content)
    @ApiResponse(responseCode = "403", description = "papel sem permissão", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "404", description = "categoria não encontrada", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "409", description = "categoria possui ocorrências", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    public ResponseEntity<Void> excluir(@Parameter(description = "id da categoria") @PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
