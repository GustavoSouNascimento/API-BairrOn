package br.com.fiap.bairron.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "bairron.jwt")
public record JwtProperties(String secret, @DefaultValue("60") long expiracaoMinutos) {
}
