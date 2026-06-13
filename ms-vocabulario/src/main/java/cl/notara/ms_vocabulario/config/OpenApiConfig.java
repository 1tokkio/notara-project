package cl.notara.ms_vocabulario.config;

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
                        .title("MS Vocabulario Inglés API")
                        .version("1.0")
                        .description("Juego de refuerzo de inglés: se muestra la definición en español y el usuario escribe la palabra en inglés. Categorías: BASICO, HOGAR, ANIMALES, ALIMENTOS, VERBOS, AVANZADO"));
    }
}
