package br.com.fiap.bairron;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
@OpenAPIDefinition(
        info = @Info(
                title = "API BairrOn",
                description = "api do mapa colaborativo de problemas do bairro. "
                        + "Todas as rotas, exceto /auth/**, GET /bairros e GET /categorias, exigem token Bearer (JWT). "
                        + "Erros seguem ProblemDetail (application/problem+json).",
                version = "1.0"
        ),
        security = @SecurityRequirement(name = "bearer-jwt")
)
@SecurityScheme(
        name = "bearer-jwt",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = "token obtido em POST /auth/login"
)

public class ApiBairrOnApplication {
    public static void main(String[] args) {
        SpringApplication.run(ApiBairrOnApplication.class, args);
    }
}
