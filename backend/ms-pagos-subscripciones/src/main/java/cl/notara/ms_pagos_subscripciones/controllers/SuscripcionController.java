package cl.notara.ms_pagos_subscripciones.controllers;

import cl.notara.ms_pagos_subscripciones.models.Suscripcion;
import cl.notara.ms_pagos_subscripciones.services.SuscripcionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Controlador REST encargado de la gestión de suscripciones.
 *
 * <p>
 * Expone los endpoints necesarios para administrar el ciclo de vida
 * de las suscripciones de usuarios, permitiendo consultar, crear,
 * cancelar y renovar planes de suscripción.
 * </p>
 *
 * <p>
 * La lógica de negocio es delegada al servicio
 * {@link SuscripcionService}, manteniendo la separación de
 * responsabilidades entre la capa de presentación y la capa de negocio.
 * </p>
 *
 * @author Notara
 * @version 1.0
 */
@RestController
@RequestMapping("/suscripciones")
public class SuscripcionController {


    /**
     * Servicio encargado de gestionar la lógica de suscripciones.
     */
    private final SuscripcionService service;


    /**
     * Constructor que inyecta el servicio de suscripciones.
     *
     * @param service servicio encargado de las operaciones de suscripción
     */
    public SuscripcionController(
            SuscripcionService service
    ) {
        this.service = service;
    }


    /**
     * Obtiene la lista completa de suscripciones registradas.
     *
     * @return lista de suscripciones existentes
     */
    @GetMapping
    public ResponseEntity<List<Suscripcion>> listar() {

        return ResponseEntity.ok(
                service.listar()
        );
    }


    /**
     * Obtiene una suscripción mediante su identificador.
     *
     * @param id identificador de la suscripción
     * @return suscripción encontrada
     */
    @GetMapping("/{id}")
    public ResponseEntity<Suscripcion> obtener(
            @PathVariable Long id
    ) {

        return ResponseEntity.ok(
                service.obtener(id)
        );
    }


    /**
     * Obtiene todas las suscripciones asociadas a un usuario.
     *
     * @param idUsuario identificador del usuario
     * @return lista de suscripciones pertenecientes al usuario
     */
    @GetMapping("/usuario/{idUsuario}")
    public ResponseEntity<List<Suscripcion>> listarPorUsuario(
            @PathVariable Long idUsuario
    ) {

        return ResponseEntity.ok(
                service.listarPorUsuario(idUsuario)
        );
    }


    /**
     * Crea una nueva suscripción.
     *
     * <p>
     * Los datos recibidos son validados mediante Bean Validation
     * utilizando la anotación {@code @Valid}.
     * </p>
     *
     * @param suscripcion información de la suscripción a crear
     * @return suscripción creada
     */
    @PostMapping
    public ResponseEntity<Suscripcion> crear(
            @Valid @RequestBody Suscripcion suscripcion
    ) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        service.crear(suscripcion)
                );
    }


    /**
     * Cancela una suscripción existente.
     *
     * @param id identificador de la suscripción
     * @return suscripción actualizada con estado cancelado
     */
    @PutMapping("/{id}/cancelar")
    public ResponseEntity<Suscripcion> cancelar(
            @PathVariable Long id
    ) {

        return ResponseEntity.ok(
                service.cancelar(id)
        );
    }


    /**
     * Renueva una suscripción actualizando su fecha de término.
     *
     * <p>
     * Recibe la nueva fecha de finalización mediante un objeto JSON
     * con la propiedad {@code fechaFin}.
     * </p>
     *
     * Ejemplo de solicitud:
     *
     * <pre>
     * {
     *     "fechaFin": "2026-12-31"
     * }
     * </pre>
     *
     * @param id identificador de la suscripción
     * @param body mapa con la nueva fecha de renovación
     * @return suscripción renovada
     */
    @PutMapping("/{id}/renovar")
    public ResponseEntity<Suscripcion> renovar(
            @PathVariable Long id,
            @RequestBody Map<String, String> body
    ) {

        LocalDate nuevaFechaFin =
                LocalDate.parse(
                        body.get("fechaFin")
                );


        return ResponseEntity.ok(
                service.renovar(
                        id,
                        nuevaFechaFin
                )
        );
    }
}
