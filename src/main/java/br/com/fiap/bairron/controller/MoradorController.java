package br.com.fiap.bairron.controller;

import br.com.fiap.bairron.dto.MoradorResponse;
import br.com.fiap.bairron.security.UsuarioLogado;
import br.com.fiap.bairron.service.MoradorService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/moradores")
public class MoradorController {

    private final MoradorService service;

    public MoradorController(MoradorService service) {
        this.service = service;
    }

    @GetMapping("/eu")
    public MoradorResponse eu(@AuthenticationPrincipal Jwt jwt) {
        return service.eu(UsuarioLogado.de(jwt));
    }

    @GetMapping
    public Page<MoradorResponse> listar(@PageableDefault(size = 10, sort = "nome") Pageable pageable) {
        return service.listar(pageable);
    }

    @GetMapping("/{id}")
    public MoradorResponse buscar(@PathVariable Long id) {
        return service.buscar(id);
    }
}
