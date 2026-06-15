package com.notara.usuarios.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración de Swagger/OpenAPI para la documentación
 * automática de los endpoints del microservicio.
 *
 * <p>
 * Esta clase registra un Bean de tipo {@link OpenAPI} que permite
 * personalizar la información mostrada en la interfaz Swagger UI,
 * incluyendo el título, versión y descripción de la API.
 * </p>
 *
 * <p>
 * La documentación generada facilita la exploración, prueba y
 * comprensión de los servicios REST expuestos por el sistema.
 * </p>
 *
 * @author Notara
 * @version 1.0
 */
@Configuration
public class SwaggerConfig {

    /**
     * Crea y configura la especificación OpenAPI del microservicio.
     *
     * <p>
     * La información configurada será visible en Swagger UI y en el
     * documento OpenAPI generado automáticamente por SpringDoc.
     * </p>
     *
     * <ul>
     *     <li><b>Título:</b> API Usuarios Notara</li>
     *     <li><b>Versión:</b> 1.0</li>
     *     <li><b>Descripción:</b> Microservicio de usuarios</li>
     * </ul>
     *
     * @return configuración personalizada de OpenAPI
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API Usuarios Notara")
                        .version("1.0")
                        .description("Microservicio de usuarios"));
    }
}
