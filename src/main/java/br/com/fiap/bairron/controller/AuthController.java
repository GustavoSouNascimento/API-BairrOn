package br.com.fiap.bairron.controller;

import br.com.fiap.bairron.dto.CadastroRequest;
import br.com.fiap.bairron.dto.LoginRequest;
import br.com.fiap.bairron.dto.MoradorResponse;
import br.com.fiap.bairron.dto.TokenResponse;
import br.com.fiap.bairron.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/auth")
@Tag(name = "autenticação", description = "cadastro de moradores e login com JWT")
public class AuthController {
    private final AuthService service;

    public AuthController(AuthService service) {
        this.service = service;
    }

    @PostMapping("/cadastro")
    @Operation(summary = "cadastrar morador (papel MORADOR)")
    @ApiResponse(responseCode = "201", description = "morador criado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = MoradorResponse.class)))
    @ApiResponse(responseCode = "400", description = "dados ou parâmetros inválidos", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "404", description = "bairro não encontrado", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "409", description = "e-mail já cadastrado", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    public ResponseEntity<MoradorResponse> cadastrar(@Valid @RequestBody CadastroRequest dados) {
        MoradorResponse morador = service.cadastrar(dados);
        URI location = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/moradores/{id}")
                .buildAndExpand(morador.id())
                .toUri();
        return ResponseEntity.created(location).body(morador);
    }

    @PostMapping("/login")
    @Operation(summary = "autenticar e receber o token JWT")
    @ApiResponse(responseCode = "200", description = "token gerado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = TokenResponse.class)))
    @ApiResponse(responseCode = "400", description = "dados ou parâmetros inválidos", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "401", description = "e-mail ou senha inválidos", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    public TokenResponse login(@Valid @RequestBody LoginRequest dados) {
        return service.login(dados);
    }
}
