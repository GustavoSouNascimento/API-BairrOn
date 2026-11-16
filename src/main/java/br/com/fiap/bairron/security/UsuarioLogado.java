package br.com.fiap.bairron.security;

import br.com.fiap.bairron.entities.Papel;
import org.springframework.security.oauth2.jwt.Jwt;

public record UsuarioLogado(Long id, Papel papel) {

    public static UsuarioLogado de(Jwt jwt) {
        Number id = (Number) jwt.getClaims().get("id");
        return new UsuarioLogado(id.longValue(), Papel.valueOf(jwt.getClaimAsString("papel")));
    }

    public boolean moderacao() {
        return papel == Papel.MODERADOR || papel == Papel.ADMIN;
    }

    public boolean podeAlterar(Long autorId) {
        return moderacao() || id.equals(autorId);
    }
}
