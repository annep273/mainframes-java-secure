package com.mainframes.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI Configuration for Swagger Documentation
 */
@Configuration
public class OpenApiConfig {

    @Value("${spring.application.name:mainframes-java-app}")
    private String applicationName;

    @Value("${app.version:1.0.0}")
    private String version;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title(applicationName + " API")
                        .version(version)
                        .description("Production-ready Spring Boot REST API for s390x OpenShift deployment")
                        .contact(new Contact()
                                .name("Mainframes Team")
                                .email("support@mainframes.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")));
    }
}
