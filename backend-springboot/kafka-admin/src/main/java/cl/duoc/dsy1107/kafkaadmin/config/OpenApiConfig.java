package cl.duoc.dsy1107.kafkaadmin.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

/** Documentacion Swagger con el boton Authorize para pegar el Bearer token. */
@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI openApi() {
        String esquema = "bearer-jwt";
        return new OpenAPI()
                .info(new Info().title("kafka-admin").version("0.0.1")
                        .description("Administracion de topicos, configuraciones y grupos de Kafka (rol Admin)"))
                .addSecurityItem(new SecurityRequirement().addList(esquema))
                .components(new Components().addSecuritySchemes(esquema,
                        new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")));
    }
}