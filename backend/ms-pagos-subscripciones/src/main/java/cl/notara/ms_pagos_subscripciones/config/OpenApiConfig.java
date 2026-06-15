package cl.notara.ms_pagos_subscripciones.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("MS Pagos y Suscripciones API")
                        .version("1.0")
                        .description("Gestión de suscripciones y pagos con notificaciones vía RabbitMQ"));
    }
}
