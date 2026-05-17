package com.diskee.diskee_project.app.swagger;

import com.diskee.diskee_project.api.exception.FileNotFoundProblem;
import com.diskee.diskee_project.api.exception.UserAlreadyExistsProblem;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Schema;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Diskee API")
                        .version("1.0.0"));
    }

    @Bean
    public OpenApiCustomizer problemSchemasCustomizer() {
        return openApi -> {
            if (openApi.getComponents() == null) {
                openApi.setComponents(new io.swagger.v3.oas.models.Components());
            }

            openApi.getComponents()
                    .addSchemas(UserAlreadyExistsProblem.SCHEMA_NAME, UserAlreadyExistsProblem.getSchema())
                    .addSchemas(FileNotFoundProblem.SCHEMA_NAME, FileNotFoundProblem.getSchema());
        };
    }
}