package br.com.fiap.bairron.controller;

import br.com.fiap.bairron.dto.MoradorResponse;
import br.com.fiap.bairron.security.UsuarioLogado;
import br.com.fiap.bairron.service.MoradorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ProblemDetail;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/moradores")
@Tag(name = "moradores", description = "consulta de moradores; a listagem é exclusiva de moderadores e administradores")
public class MoradorController {

    private final MoradorService service;

    public MoradorController(MoradorService service) {
        this.service = service;
    }

    @GetMapping("/eu")
    @Operation(summary = "consultar os dados do morador logado")
    @ApiResponse(responseCode = "200", description = "morador logado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = MoradorResponse.class)))
    @ApiResponse(responseCode = "401", description = "token ausente, inválido ou expirado", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    public MoradorResponse eu(@AuthenticationPrincipal Jwt jwt) {
        return service.eu(UsuarioLogado.de(jwt));
    }

    @GetMapping
    @Operation(summary = "listar moradores com paginação e ordenação (MODERADOR ou ADMIN)")
    @ApiResponse(responseCode = "200", description = "página de moradores")
    @ApiResponse(responseCode = "400", description = "dados ou parâmetros inválidos", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "403", description = "papel sem permissão", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    public PagedModel<EntityModel<MoradorResponse>> listar(
            @ParameterObject @PageableDefault(size = 10, sort = "nome") Pageable pageable,
            @Parameter(hidden = true) PagedResourcesAssembler<MoradorResponse> pagedAssembler) {
        return pagedAssembler.toModel(service.listar(pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "consultar morador por id (MODERADOR ou ADMIN)")
    @ApiResponse(responseCode = "200", description = "morador encontrado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = MoradorResponse.class)))
    @ApiResponse(responseCode = "403", description = "papel sem permissão", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "404", description = "morador não encontrado", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    public MoradorResponse buscar(@Parameter(description = "id do morador") @PathVariable Long id) {
        return service.buscar(id);
    }
}
