package cl.notara.ms_notificaciones.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración de Swagger / OpenAPI para el microservicio de notificaciones.
 *
 * <p>
 * Este microservicio es orientado a eventos (consumer RabbitMQ) y no expone
 * endpoints REST de negocio. La documentación cubre únicamente el endpoint
 * de salud ({@code /health}) disponible para monitoreo.
 * </p>
 *
 * <p>
 * La documentación está disponible en:
 * {@code http://localhost:8085/swagger-ui.html}
 * </p>
 *
 * @author Notara
 * @version 1.0
 */
@OpenAPIDefinition(
    info = @Info(
        title = "ms-notificaciones API",
        version = "1.0.0",
        description = "Microservicio de notificaciones por email. " +
                      "Consume eventos de suscripción desde RabbitMQ y envía " +
                      "correos HTML a los usuarios."
    )
)
@Configuration
public class OpenApiConfig {
}
