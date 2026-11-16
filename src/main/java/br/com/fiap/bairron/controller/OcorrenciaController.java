package br.com.fiap.bairron.controller;

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

    public OcorrenciaController(OcorrenciaService service) {
        this.service = service;
    }

    @GetMapping
    public Page<OcorrenciaResponse> listar(
            @RequestParam(required = false) Long bairroId,
            @RequestParam(required = false) Long categoriaId,
            @RequestParam(required = false) StatusOcorrencia status,
            @RequestParam(required = false) Prioridade prioridade,
            @RequestParam(required = false) String texto,
            @PageableDefault(size = 10, sort = "pontuacao", direction = Sort.Direction.DESC) Pageable pageable) {
        return service.listar(bairroId, categoriaId, status, prioridade, texto, pageable);
    }

    @GetMapping("/{id}")
    public OcorrenciaResponse buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    @PostMapping
    public ResponseEntity<OcorrenciaResponse> cadastrar(@Valid @RequestBody OcorrenciaRequest dados,
                                                        @AuthenticationPrincipal Jwt jwt) {
        OcorrenciaResponse criada = service.cadastrar(dados, UsuarioLogado.de(jwt));
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(criada.id()).toUri();
        return ResponseEntity.created(location).body(criada);
    }

    @PutMapping("/{id}")
    public OcorrenciaResponse atualizar(@PathVariable Long id, @Valid @RequestBody OcorrenciaRequest dados,
                                        @AuthenticationPrincipal Jwt jwt) {
        return service.atualizar(id, dados, UsuarioLogado.de(jwt));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        service.excluir(id, UsuarioLogado.de(jwt));
        return ResponseEntity.noContent().build();
    }
}
