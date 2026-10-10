package com.donorconnect.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "bearerAuth";

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("DonorConnect API")
                        .version("1.0.0")
                        .description("DonorConnect is a blood donor discovery and coordination platform.\n\n" +
                                "IMPORTANT DISCLAIMER: Potential donor matches identified by this system represent " +
                                "coordination opportunities only. The platform does NOT determine medical eligibility, " +
                                "compatibility, or clinical approval. Final donation confirmation and medical decisions " +
                                "belong exclusively to authorized hospitals, blood banks, and healthcare professionals.")
                        .contact(new Contact()
                                .name("DonorConnect Development Team")
                                .email("support@donorconnect.example.com")))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME,
                                new SecurityScheme()
                                        .name(SECURITY_SCHEME_NAME)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Provide valid JWT Bearer token obtained from /api/auth/login or /api/auth/register")));
    }
}
