package com.example.peakly.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        final String securitySchemeName = "JWT";

        return new OpenAPI()
                .info(new Info()
                        .title("Eatsfine API 명세서")
                        .description("Eatsfine 프로젝트의 Swagger 문서입니다.")
                        .version("1.0.0"))

                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Local")))

                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name("Authorization")
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .in(SecurityScheme.In.HEADER)));
    }
}