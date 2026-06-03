package cl.notara.ms_notas_metas.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración de OpenAPI y Swagger para el microservicio
 * de notas y metas.
 *
 * <p>
 * Esta clase define los metadatos de la documentación automática
 * generada mediante OpenAPI, permitiendo describir la API,
 * su versión y propósito dentro de la arquitectura del sistema.
 * </p>
 *
 * <p>
 * La documentación puede visualizarse mediante Swagger UI,
 * facilitando la exploración y prueba de los endpoints
 * disponibles en el microservicio.
 * </p>
 *
 * @author Notara
 * @version 1.0
 */
@Configuration
public class OpenApiConfig {

    /**
     * Configura la información principal de la especificación OpenAPI.
     *
     * <p>
     * Define el título, versión y descripción que serán mostrados
     * en la interfaz Swagger UI y en el documento OpenAPI generado.
     * </p>
     *
     * @return configuración personalizada de OpenAPI
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("MS Notas y Metas API")
                        .version("1.0")
                        .description("Documentación de la API para notas y metas"));
    }
}
