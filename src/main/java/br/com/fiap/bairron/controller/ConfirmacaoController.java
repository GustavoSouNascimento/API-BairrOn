package br.com.fiap.bairron.controller;

import br.com.fiap.bairron.dto.ConfirmacaoRequest;
import br.com.fiap.bairron.dto.ConfirmacaoResponse;
import br.com.fiap.bairron.security.UsuarioLogado;
import br.com.fiap.bairron.service.ConfirmacaoService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/ocorrencias/{ocorrenciaId}/confirmacoes")
public class ConfirmacaoController {
    private final ConfirmacaoService service;

    public ConfirmacaoController(ConfirmacaoService service) {
        this.service = service;
    }

    @GetMapping
    public PagedModel<EntityModel<ConfirmacaoResponse>> listar(
            @PathVariable Long ocorrenciaId,
            @PageableDefault(size = 10, sort = "confirmadaEm", direction = Sort.Direction.DESC) Pageable pageable,
            PagedResourcesAssembler<ConfirmacaoResponse> pagedAssembler) {
        return pagedAssembler.toModel(service.listar(ocorrenciaId, pageable), this::modelo);
    }

    @PostMapping
    public ResponseEntity<EntityModel<ConfirmacaoResponse>> registrar(
            @PathVariable Long ocorrenciaId,
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
