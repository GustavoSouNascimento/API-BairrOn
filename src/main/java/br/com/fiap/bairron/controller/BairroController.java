package br.com.fiap.bairron.controller;

import br.com.fiap.bairron.assembler.BairroModelAssembler;
import br.com.fiap.bairron.dto.BairroRequest;
import br.com.fiap.bairron.dto.BairroResponse;
import br.com.fiap.bairron.service.BairroService;
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
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/bairros")
@Tag(name = "bairros", description = "bairros atendidos pelo mapa; consulta pública, escrita só para moderadores e administradores")
public class BairroController {

    private final BairroService service;
    private final BairroModelAssembler assembler;

    public BairroController(BairroService service, BairroModelAssembler assembler) {
        this.service = service;
        this.assembler = assembler;
    }

    @GetMapping
    @SecurityRequirements
    @Operation(summary = "listar bairros com paginação, ordenação e filtro opcional por nome")
    @ApiResponse(responseCode = "200", description = "página de bairros; pode estar vazia")
    @ApiResponse(responseCode = "400", description = "dados ou parâmetros inválidos", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    public PagedModel<EntityModel<BairroResponse>> listar(
            @Parameter(description = "trecho do nome, sem diferenciar maiúsculas e minúsculas")
            @RequestParam(required = false) String nome,
            @ParameterObject @PageableDefault(size = 10, sort = "nome") Pageable pageable,
            @Parameter(hidden = true) PagedResourcesAssembler<BairroResponse> pagedAssembler) {
        return pagedAssembler.toModel(service.listar(nome, pageable), assembler);
    }

    @GetMapping("/{id}")
    @SecurityRequirements
    @Operation(summary = "consultar bairro por id")
    @ApiResponse(responseCode = "200", description = "bairro encontrado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BairroResponse.class)))
    @ApiResponse(responseCode = "404", description = "bairro não encontrado", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    public EntityModel<BairroResponse> buscar(@Parameter(description = "id do bairro") @PathVariable Long id) {
        return assembler.toModel(service.buscar(id));
    }

    @PostMapping
    @Operation(summary = "cadastrar bairro (MODERADOR ou ADMIN)")
    @ApiResponse(responseCode = "201", description = "bairro criado; endereço no cabeçalho location", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BairroResponse.class)))
    @ApiResponse(responseCode = "400", description = "dados ou parâmetros inválidos", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "403", description = "papel sem permissão", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "409", description = "bairro já cadastrado nesta cidade", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    public ResponseEntity<EntityModel<BairroResponse>> cadastrar(@Valid @RequestBody BairroRequest dados) {
        EntityModel<BairroResponse> modelo = assembler.toModel(service.cadastrar(dados));
        return ResponseEntity.created(modelo.getRequiredLink("self").toUri()).body(modelo);
    }

    @PutMapping("/{id}")
    @Operation(summary = "atualizar todos os campos do bairro (MODERADOR ou ADMIN)")
    @ApiResponse(responseCode = "200", description = "bairro atualizado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BairroResponse.class)))
    @ApiResponse(responseCode = "400", description = "dados ou parâmetros inválidos", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "403", description = "papel sem permissão", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "404", description = "bairro não encontrado", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "409", description = "bairro já cadastrado nesta cidade", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    public EntityModel<BairroResponse> atualizar(
            @Parameter(description = "id do bairro") @PathVariable Long id,
            @Valid @RequestBody BairroRequest dados) {
        return assembler.toModel(service.atualizar(id, dados));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "excluir bairro sem moradores nem ocorrências (MODERADOR ou ADMIN)")
    @ApiResponse(responseCode = "204", description = "bairro excluído", content = @Content)
    @ApiResponse(responseCode = "403", description = "papel sem permissão", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "404", description = "bairro não encontrado", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "409", description = "bairro possui moradores ou ocorrências", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    public ResponseEntity<Void> excluir(@Parameter(description = "id do bairro") @PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}