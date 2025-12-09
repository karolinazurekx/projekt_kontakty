package com.example.contacts.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAPIConfig {

    @Bean
    public OpenAPI contactsApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Contacts API")
                        .description("REST API for managing contacts with JWT authentication")
                        .version("1.0.0")
                        .license(new License().name("MIT License"))
                )
                // Dodajemy security do wszystkich endpointów
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                // Definiujemy schemat JWT Bearer
                .components(new Components()
                        .addSecuritySchemes("bearerAuth",
                                new SecurityScheme()
                                        .name("bearerAuth")
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                        )
                );
    }
}
