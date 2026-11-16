package br.com.fiap.bairron.controller;

import br.com.fiap.bairron.dto.CadastroRequest;
import br.com.fiap.bairron.dto.LoginRequest;
import br.com.fiap.bairron.dto.MoradorResponse;
import br.com.fiap.bairron.dto.TokenResponse;
import br.com.fiap.bairron.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthService service;

    public AuthController(AuthService service) {
        this.service = service;
    }

    @PostMapping("/cadastro")
    public ResponseEntity<MoradorResponse> cadastrar(@Valid @RequestBody CadastroRequest dados) {
        MoradorResponse morador = service.cadastrar(dados);
        URI location = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/moradores/{id}")
                .buildAndExpand(morador.id())
                .toUri();
        return ResponseEntity.created(location).body(morador);
    }

    @PostMapping("/login")
    public TokenResponse login(@Valid @RequestBody LoginRequest dados) {
        return service.login(dados);
    }
}
