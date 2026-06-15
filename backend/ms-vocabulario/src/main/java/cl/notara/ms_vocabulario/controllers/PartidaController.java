package cl.notara.ms_vocabulario.controllers;

import cl.notara.ms_vocabulario.dto.*;
import cl.notara.ms_vocabulario.models.Partida;
import cl.notara.ms_vocabulario.services.PartidaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST encargado de la gestión de partidas de vocabulario.
 *
 * <p>
 * Permite administrar el flujo de partidas de aprendizaje, incluyendo
 * el inicio de una partida, obtención de preguntas, envío de respuestas,
 * abandono de partidas y consulta del historial de partidas de un usuario.
 * </p>
 *
 * <p>
 * La lógica de negocio es delegada a {@link PartidaService}, manteniendo
 * una separación entre la capa de presentación y la capa de servicios.
 * </p>
 *
 * @author Notara
 * @version 1.0
 */
@RestController
@RequestMapping("/vocabulario/partidas")
public class PartidaController {

    /**
     * Servicio encargado de la lógica de negocio de las partidas.
     */
    private final PartidaService service;

    /**
     * Constructor que inyecta el servicio de partidas.
     *
     * @param service servicio de gestión de partidas
     */
    public PartidaController(PartidaService service) {
        this.service = service;
    }

    /**
     * Inicia una nueva partida de vocabulario.
     *
     * <p>
     * Recibe los parámetros necesarios para crear una partida
     * y devuelve la primera pregunta generada.
     * </p>
     *
     * @param req datos necesarios para iniciar la partida
     * @return pregunta inicial de la partida creada
     */
    @PostMapping
    public ResponseEntity<PreguntaDTO> iniciar(
            @Valid @RequestBody IniciarPartidaRequest req
    ) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(service.iniciar(req));
    }

    /**
     * Obtiene la pregunta actual de una partida.
     *
     * @param id identificador de la partida
     * @return pregunta actual asociada a la partida
     */
    @GetMapping("/{id}/pregunta")
    public ResponseEntity<PreguntaDTO> preguntaActual(
            @PathVariable Long id
    ) {

        return ResponseEntity.ok(
                service.obtenerPreguntaActual(id)
        );
    }

    /**
     * Procesa la respuesta enviada por un usuario para una pregunta.
     *
     * <p>
     * Evalúa la respuesta y actualiza el progreso de la partida.
     * </p>
     *
     * @param id identificador de la partida
     * @param req información de la respuesta enviada
     * @return resultado de la respuesta procesada
     */
    @PostMapping("/{id}/responder")
    public ResponseEntity<RespuestaDTO> responder(
            @PathVariable Long id,
            @Valid @RequestBody ResponderRequest req
    ) {

        return ResponseEntity.ok(
                service.responder(id, req)
        );
    }

    /**
     * Permite abandonar una partida activa.
     *
     * @param id identificador de la partida
     * @return respuesta sin contenido HTTP 204
     */
    @PutMapping("/{id}/abandonar")
    public ResponseEntity<Void> abandonar(
            @PathVariable Long id
    ) {

        service.abandonar(id);

        return ResponseEntity.noContent().build();
    }

    /**
     * Obtiene una partida específica mediante su identificador.
     *
     * @param id identificador de la partida
     * @return partida encontrada
     */
    @GetMapping("/{id}")
    public ResponseEntity<Partida> obtener(
            @PathVariable Long id
    ) {

        return ResponseEntity.ok(
                service.obtener(id)
        );
    }

    /**
     * Obtiene el historial de partidas realizadas por un usuario.
     *
     * @param idUsuario identificador del usuario
     * @return lista de partidas asociadas al usuario
     */
    @GetMapping("/usuario/{idUsuario}")
    public ResponseEntity<List<Partida>> historial(
            @PathVariable Long idUsuario
    ) {

        return ResponseEntity.ok(
                service.historialUsuario(idUsuario)
        );
    }
}