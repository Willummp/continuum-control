package br.infnet.continuum.control.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI continuumOpenAPI() {
        return new OpenAPI().info(new Info()
                .title("Continuum Control")
                .version("0.0.1")
                .description("Sistema de Gestão e Controle da Integridade Temporal"));
    }
}
