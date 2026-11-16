package br.com.fiap.bairron.controller;

import br.com.fiap.bairron.assembler.CategoriaModelAssembler;
import br.com.fiap.bairron.dto.CategoriaRequest;
import br.com.fiap.bairron.dto.CategoriaResponse;
import br.com.fiap.bairron.service.CategoriaService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/categorias")
public class CategoriaController {
    private final CategoriaService service;
    private final CategoriaModelAssembler assembler;

    public CategoriaController(CategoriaService service, CategoriaModelAssembler assembler) {
        this.service = service;
        this.assembler = assembler;
    }

    @GetMapping
    public PagedModel<EntityModel<CategoriaResponse>> listar(
            @RequestParam(required = false) String nome,
            @PageableDefault(size = 10, sort = "nome") Pageable pageable,
            PagedResourcesAssembler<CategoriaResponse> pagedAssembler) {
        return pagedAssembler.toModel(service.listar(nome, pageable), assembler);
    }

    @GetMapping("/{id}")
    public EntityModel<CategoriaResponse> buscar(@PathVariable Long id) {
        return assembler.toModel(service.buscar(id));
    }

    @PostMapping
    public ResponseEntity<EntityModel<CategoriaResponse>> cadastrar(@Valid @RequestBody CategoriaRequest dados) {
        EntityModel<CategoriaResponse> modelo = assembler.toModel(service.cadastrar(dados));
        return ResponseEntity.created(modelo.getRequiredLink("self").toUri()).body(modelo);
    }

    @PutMapping("/{id}")
    public EntityModel<CategoriaResponse> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody CategoriaRequest dados) {
        return assembler.toModel(service.atualizar(id, dados));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
