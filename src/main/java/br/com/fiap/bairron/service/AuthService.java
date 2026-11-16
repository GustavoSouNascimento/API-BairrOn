package br.com.fiap.bairron.service;

import br.com.fiap.bairron.dto.CadastroRequest;
import br.com.fiap.bairron.dto.LoginRequest;
import br.com.fiap.bairron.dto.MoradorResponse;
import br.com.fiap.bairron.dto.TokenResponse;
import br.com.fiap.bairron.entities.Bairro;
import br.com.fiap.bairron.entities.Morador;
import br.com.fiap.bairron.entities.Papel;
import br.com.fiap.bairron.exception.BairroNaoEncontradoException;
import br.com.fiap.bairron.exception.ConflitoException;
import br.com.fiap.bairron.exception.CredenciaisInvalidasException;
import br.com.fiap.bairron.repository.BairroRepository;
import br.com.fiap.bairron.repository.MoradorRepository;
import br.com.fiap.bairron.security.TokenService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final MoradorRepository moradorRepository;
    private final BairroRepository bairroRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    public AuthService(MoradorRepository moradorRepository, BairroRepository bairroRepository,
                       PasswordEncoder passwordEncoder, TokenService tokenService) {
        this.moradorRepository = moradorRepository;
        this.bairroRepository = bairroRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
    }
    
    @Transactional
    public MoradorResponse cadastrar(CadastroRequest dados) {
        String email = dados.email().trim().toLowerCase();
        if (moradorRepository.existsByEmailIgnoreCase(email)) {
            throw new ConflitoException("Já existe um morador cadastrado com este e-mail.");
        }
        Bairro bairro = bairroRepository.findById(dados.bairroId())
                .orElseThrow(() -> new BairroNaoEncontradoException(dados.bairroId()));
        Morador morador = new Morador();
        morador.setNome(dados.nome().trim());
        morador.setEmail(email);
        morador.setSenha(passwordEncoder.encode(dados.senha()));
        morador.setPapel(Papel.MORADOR);
        morador.setBairro(bairro);
        return MoradorService.resposta(moradorRepository.save(morador));
    }

    @Transactional(readOnly = true)
    public TokenResponse login(LoginRequest dados) {
        Morador morador = moradorRepository.findByEmailIgnoreCase(dados.email().trim())
                .orElseThrow(CredenciaisInvalidasException::new);
        if (!passwordEncoder.matches(dados.senha(), morador.getSenha())) {
            throw new CredenciaisInvalidasException();
        }
        return tokenService.gerar(morador);
    }
}
