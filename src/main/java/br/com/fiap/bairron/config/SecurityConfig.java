package br.com.fiap.bairron.config;

import br.com.fiap.bairron.security.ProblemaSegurancaHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.security.autoconfigure.actuate.web.servlet.EndpointRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain filtros(HttpSecurity http, JwtAuthenticationConverter conversor,
                                ProblemaSegurancaHandler problema,
                                CorsConfigurationSource corsConfigurationSource) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(c -> c.configurationSource(corsConfigurationSource))
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(regras -> regras
                        .requestMatchers(EndpointRequest.to("health")).permitAll()
                        .requestMatchers(EndpointRequest.to("info", "metrics")).hasRole("ADMIN")
                        .requestMatchers(EndpointRequest.toAnyEndpoint()).hasRole("ADMIN")
                        .requestMatchers("/auth/**").permitAll()
                        .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/bairros", "/bairros/*", "/categorias", "/categorias/*").permitAll()
                        .requestMatchers(HttpMethod.POST, "/bairros", "/categorias").hasAnyRole("MODERADOR", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/bairros/*", "/categorias/*").hasAnyRole("MODERADOR", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/bairros/*", "/categorias/*").hasAnyRole("MODERADOR", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/moradores/eu").authenticated()
                        .requestMatchers(HttpMethod.GET, "/moradores", "/moradores/*").hasAnyRole("MODERADOR", "ADMIN")
                        .anyRequest().authenticated())
                .exceptionHandling(e -> e.authenticationEntryPoint(problema).accessDeniedHandler(problema))
                .oauth2ResourceServer(o -> o
                        .jwt(j -> j.jwtAuthenticationConverter(conversor))
                        .authenticationEntryPoint(problema)
                        .accessDeniedHandler(problema));
        return http.build();
    }

    @Bean
    JwtAuthenticationConverter conversorJwt() {
        JwtGrantedAuthoritiesConverter autoridades = new JwtGrantedAuthoritiesConverter();
        autoridades.setAuthoritiesClaimName("papel");
        autoridades.setAuthorityPrefix("ROLE_");
        JwtAuthenticationConverter conversor = new JwtAuthenticationConverter();
        conversor.setJwtGrantedAuthoritiesConverter(autoridades);
        return conversor;
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource(@Value("${bairron.cors.origens:*}") String[] origens) {
        CorsConfiguration configuracao = new CorsConfiguration();
        configuracao.setAllowedOriginPatterns(List.of(origens));
        configuracao.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuracao.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept"));
        configuracao.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource fonte = new UrlBasedCorsConfigurationSource();
        fonte.registerCorsConfiguration("/**", configuracao);
        return fonte;
    }
}
