package com.healthapp.backend.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.Components;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger/OpenAPI configuration for API documentation.
 * Auto-detects production environment and configures HTTPS server URL.
 * Accessible at /swagger-ui/index.html
 */
@Configuration
public class SwaggerConfig {

    @Value("${server.forward-headers-strategy:framework}")
    private String forwardHeadersStrategy;

    /**
     * Configures OpenAPI documentation with JWT Bearer authentication scheme.
     * Automatically uses HTTPS in production (Cloud Run) and HTTP in local development.
     */
    @Bean
    public OpenAPI customOpenAPI() {
        SecurityScheme securityScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .in(SecurityScheme.In.HEADER)
                .name("Authorization");

        SecurityRequirement securityRequirement = new SecurityRequirement()
                .addList("bearerAuth");

        // Determine if we're in production (Cloud Run sets K_SERVICE environment variable)
        String cloudRunService = System.getenv("K_SERVICE");

        OpenAPI openAPI = new OpenAPI();

        if (cloudRunService != null) {
            // Production: Only show cloud URL
            openAPI.addServersItem(new Server()
                    .url("https://healthapp-backend-v2-186862202342.us-central1.run.app")
                    .description("Production Server (GCP Cloud Run)"));
        } else {
            // Local: Show both options
            openAPI.addServersItem(new Server()
                    .url("http://localhost:8080")
                    .description("Local Development Server"));
            openAPI.addServersItem(new Server()
                    .url("https://healthapp-backend-v2-186862202342.us-central1.run.app")
                    .description("Production Server (GCP Cloud Run)"));
        }

        return openAPI
                .info(new Info()
                        .title("Healthcare Platform APIs")
                        .description("Healthcare Platform APIs")
                        .version("2.0.0")
                        .contact(new Contact()
                                .name("contact")
                                .email("health.services.platform@gmail.com")))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", securityScheme))
                .addSecurityItem(securityRequirement);
    }
}