package cl.notara.ms_notas_metas.controllers;

import cl.notara.ms_notas_metas.client.UsuarioClient;
import cl.notara.ms_notas_metas.models.Nota;
import cl.notara.ms_notas_metas.services.NotaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST encargado de la gestión de notas.
 *
 * <p>
 * Proporciona endpoints para crear, consultar, actualizar y eliminar
 * notas registradas por los usuarios del sistema.
 * </p>
 *
 * <p>
 * Las solicitudes recibidas son procesadas mediante el servicio
 * {@link NotaService}, encargado de la lógica de negocio asociada
 * a las notas.
 * </p>
 *
 * <p>
 * Además, dispone de un cliente Feign ({@link UsuarioClient}) para
 * interactuar con el microservicio de usuarios cuando sea necesario.
 * </p>
 *
 * @author Notara
 * @version 1.0
 */
@RestController
@RequestMapping("/notas")
@Tag(name = "Notas", description = "Operaciones relacionadas con notas")
public class NotaController {

    /**
     * Servicio encargado de la lógica de negocio de las notas.
     */
    private final NotaService notaService;

    /**
     * Cliente Feign para la comunicación con el microservicio de usuarios.
     */
    private final UsuarioClient usuarioCliente;

    /**
     * Constructor que inyecta las dependencias necesarias.
     *
     * @param notaService servicio de notas
     * @param usuarioCliente cliente de usuarios
     */
    public NotaController(
            NotaService notaService,
            UsuarioClient usuarioCliente
    ) {
        this.notaService = notaService;
        this.usuarioCliente = usuarioCliente;
    }

    /**
     * Obtiene todas las notas registradas en el sistema.
     *
     * @return lista de notas
     */
    @GetMapping
    @Operation(summary = "Listar todas las notas")
    public ResponseEntity<List<Nota>> listar() {
        return ResponseEntity.ok(notaService.listar());
    }

    /**
     * Crea una nueva nota.
     *
     * <p>
     * La información recibida es validada antes de ser almacenada
     * en la base de datos.
     * </p>
     *
     * @param nota datos de la nota a crear
     * @return nota creada
     */
    @PostMapping
    @Operation(summary = "Crear nota")
    public ResponseEntity<Nota> crear(@Valid @RequestBody Nota nota) {
        Nota nuevaNota = notaService.guardar(nota);
        return ResponseEntity.status(201).body(nuevaNota);
    }

    /**
     * Obtiene una nota específica utilizando su identificador.
     *
     * @param id identificador de la nota
     * @return nota encontrada
     */
    @GetMapping("/{id}")
    @Operation(summary = "Obtener nota por ID")
    public ResponseEntity<Nota> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(notaService.obtener(id));
    }

    /**
     * Obtiene todas las notas asociadas a un usuario.
     *
     * @param idUsuario identificador del usuario
     * @return lista de notas pertenecientes al usuario
     */
    @GetMapping("/usuario/{idUsuario}")
    @Operation(summary = "Obtener notas por usuario")
    public ResponseEntity<List<Nota>> obtenerPorUsuario(
            @PathVariable Long idUsuario
    ) {
        return ResponseEntity.ok(
                notaService.obtenerPorUsuario(idUsuario)
        );
    }

    /**
     * Elimina una nota existente.
     *
     * @param id identificador de la nota a eliminar
     * @return respuesta sin contenido (HTTP 204)
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar nota")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        notaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Actualiza los datos de una nota existente.
     *
     * @param id identificador de la nota
     * @param nota nueva información de la nota
     * @return nota actualizada
     */
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar nota")
    public ResponseEntity<Nota> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody Nota nota) {

        return ResponseEntity.ok(
                notaService.actualizar(id, nota)
        );
    }
}
