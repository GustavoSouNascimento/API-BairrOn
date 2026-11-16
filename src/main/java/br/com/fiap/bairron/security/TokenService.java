package br.com.fiap.bairron.security;

import br.com.fiap.bairron.config.JwtProperties;
import br.com.fiap.bairron.dto.TokenResponse;
import br.com.fiap.bairron.entities.Morador;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

@Service
public class TokenService {

    private final JwtEncoder encoder;
    private final JwtProperties propriedades;

    public TokenService(JwtEncoder encoder, JwtProperties propriedades) {
        this.encoder = encoder;
        this.propriedades = propriedades;
    }

    public TokenResponse gerar(Morador morador) {
        Instant agora = Instant.now();
        Instant expiraEm = agora.plus(Duration.ofMinutes(propriedades.expiracaoMinutos()));
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("api-bairron")
                .issuedAt(agora)
                .expiresAt(expiraEm)
                .subject(morador.getEmail())
                .claim("id", morador.getId())
                .claim("papel", morador.getPapel().name())
                .build();
        JwsHeader cabecalho = JwsHeader.with(MacAlgorithm.HS256).build();
        String token = encoder.encode(JwtEncoderParameters.from(cabecalho, claims)).getTokenValue();
        return new TokenResponse(token, "Bearer", expiraEm, morador.getPapel());
    }
}
