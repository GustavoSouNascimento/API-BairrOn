package br.com.fiap.bairron.controller;

import br.com.fiap.bairron.assembler.OcorrenciaModelAssembler;
import br.com.fiap.bairron.dto.OcorrenciaRequest;
import br.com.fiap.bairron.dto.OcorrenciaResponse;
import br.com.fiap.bairron.entities.Prioridade;
import br.com.fiap.bairron.entities.StatusOcorrencia;
import br.com.fiap.bairron.security.UsuarioLogado;
import br.com.fiap.bairron.service.OcorrenciaService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/ocorrencias")
public class OcorrenciaController {
    private final OcorrenciaService service;
    private final OcorrenciaModelAssembler assembler;

    public OcorrenciaController(OcorrenciaService service, OcorrenciaModelAssembler assembler) {
        this.service = service;
        this.assembler = assembler;
    }

    @GetMapping
    public PagedModel<EntityModel<OcorrenciaResponse>> listar(
            @RequestParam(required = false) Long bairroId,
            @RequestParam(required = false) Long categoriaId,
            @RequestParam(required = false) StatusOcorrencia status,
            @RequestParam(required = false) Prioridade prioridade,
            @RequestParam(required = false) String texto,
            @PageableDefault(size = 10, sort = "pontuacao", direction = Sort.Direction.DESC) Pageable pageable,
            PagedResourcesAssembler<OcorrenciaResponse> pagedAssembler,
            @AuthenticationPrincipal Jwt jwt) {
        UsuarioLogado usuario = UsuarioLogado.de(jwt);
        return pagedAssembler.toModel(
                service.listar(bairroId, categoriaId, status, prioridade, texto, pageable),
                ocorrencia -> assembler.toModel(ocorrencia, usuario));
    }

    @GetMapping("/{id}")
    public EntityModel<OcorrenciaResponse> buscar(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt) {
        return assembler.toModel(service.buscar(id), UsuarioLogado.de(jwt));
    }

    @PostMapping
    public ResponseEntity<EntityModel<OcorrenciaResponse>> cadastrar(
            @Valid @RequestBody OcorrenciaRequest dados,
            @AuthenticationPrincipal Jwt jwt) {
        UsuarioLogado usuario = UsuarioLogado.de(jwt);
        EntityModel<OcorrenciaResponse> modelo = assembler.toModel(service.cadastrar(dados, usuario), usuario);
        return ResponseEntity.created(modelo.getRequiredLink("self").toUri()).body(modelo);
    }

    @PutMapping("/{id}")
    public EntityModel<OcorrenciaResponse> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody OcorrenciaRequest dados,
            @AuthenticationPrincipal Jwt jwt) {
        UsuarioLogado usuario = UsuarioLogado.de(jwt);
        return assembler.toModel(service.atualizar(id, dados, usuario), usuario);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt) {
        service.excluir(id, UsuarioLogado.de(jwt));
        return ResponseEntity.noContent().build();
    }
}
