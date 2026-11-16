package br.com.fiap.bairron.controller;

import br.com.fiap.bairron.assembler.BairroModelAssembler;
import br.com.fiap.bairron.dto.BairroRequest;
import br.com.fiap.bairron.dto.BairroResponse;
import br.com.fiap.bairron.service.BairroService;
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
@RequestMapping("/bairros")
public class BairroController {

    private final BairroService service;
    private final BairroModelAssembler assembler;

    public BairroController(BairroService service, BairroModelAssembler assembler) {
        this.service = service;
        this.assembler = assembler;
    }

    @GetMapping
    public PagedModel<EntityModel<BairroResponse>> listar(
            @RequestParam(required = false) String nome,
            @PageableDefault(size = 10, sort = "nome") Pageable pageable,
            PagedResourcesAssembler<BairroResponse> pagedAssembler) {
        return pagedAssembler.toModel(service.listar(nome, pageable), assembler);
    }

    @GetMapping("/{id}")
    public EntityModel<BairroResponse> buscar(@PathVariable Long id) {
        return assembler.toModel(service.buscar(id));
    }

    @PostMapping
    public ResponseEntity<EntityModel<BairroResponse>> cadastrar(@Valid @RequestBody BairroRequest dados) {
        EntityModel<BairroResponse> modelo = assembler.toModel(service.cadastrar(dados));
        return ResponseEntity.created(modelo.getRequiredLink("self").toUri()).body(modelo);
    }

    @PutMapping("/{id}")
    public EntityModel<BairroResponse> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody BairroRequest dados) {
        return assembler.toModel(service.atualizar(id, dados));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
