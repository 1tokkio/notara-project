package cl.notara.ms_notificaciones.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Controlador de estado del microservicio de notificaciones.
 *
 * <p>
 * Expone un endpoint de salud para verificar que el servicio
 * esta operativo. El microservicio es orientado a eventos (consumer RabbitMQ)
 * y no expone endpoints de negocio REST.
 * </p>
 *
 * @author Notara
 * @version 1.0
 */
@RestController
@RequestMapping("/health")
@Tag(name = "Health", description = "Estado del microservicio")
public class HealthController {

    /**
     * Retorna el estado actual del microservicio.
     *
     * @return mapa con estado, nombre del servicio y timestamp
     */
    @GetMapping
    @Operation(
        summary = "Estado del servicio",
        description = "Verifica que el microservicio de notificaciones esta activo"
    )
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "service", "ms-notificaciones",
            "timestamp", LocalDateTime.now().toString(),
            "description", "Consumer RabbitMQ activo — SUSCRIPCION_CREADA/CANCELADA/RENOVADA"
        ));
    }
}
