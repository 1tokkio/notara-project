package cl.notara.ms_notas_metas.controllers;

import cl.notara.ms_notas_metas.models.Meta;
import cl.notara.ms_notas_metas.services.MetaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST encargado de la gestión de metas.
 *
 * <p>
 * Proporciona endpoints para crear, consultar, actualizar y eliminar
 * metas asociadas a los usuarios del sistema.
 * </p>
 *
 * <p>
 * Todas las operaciones son delegadas al servicio {@link MetaService},
 * encargado de implementar la lógica de negocio correspondiente.
 * </p>
 *
 * @author Notara
 * @version 1.0
 */
@RestController
@RequestMapping("/metas")
@Tag(name = "Metas", description = "Operaciones relacionadas con metas")
public class MetaController {

    /**
     * Servicio encargado de la gestión de metas.
     */
    private final MetaService metaService;

    /**
     * Constructor que inyecta el servicio de metas.
     *
     * @param metaService servicio de gestión de metas
     */
    public MetaController(MetaService metaService) {
        this.metaService = metaService;
    }

    /**
     * Obtiene la lista completa de metas registradas.
     *
     * @return lista de metas
     */
    @GetMapping
    @Operation(summary = "Listar todas las metas")
    public ResponseEntity<List<Meta>> listar() {
        return ResponseEntity.ok(metaService.listar());
    }

    /**
     * Crea una nueva meta en el sistema.
     *
     * <p>
     * La información recibida es validada antes de ser almacenada.
     * </p>
     *
     * @param meta datos de la meta a crear
     * @return meta creada
     */
    @PostMapping
    @Operation(summary = "Crear meta")
    public ResponseEntity<Meta> crear(@Valid @RequestBody Meta meta) {
        Meta nuevaMeta = metaService.guardar(meta);
        return ResponseEntity.status(201).body(nuevaMeta);
    }

    /**
     * Obtiene una meta específica utilizando su identificador.
     *
     * @param id identificador de la meta
     * @return meta encontrada
     */
    @GetMapping("/{id}")
    @Operation(summary = "Obtener meta por ID")
    public ResponseEntity<Meta> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(metaService.obtener(id));
    }

    /**
     * Obtiene todas las metas asociadas a un usuario.
     *
     * @param idUsuario identificador del usuario
     * @return lista de metas pertenecientes al usuario
     */
    @GetMapping("/usuario/{idUsuario}")
    @Operation(summary = "Obtener metas por usuario")
    public ResponseEntity<List<Meta>> obtenerPorUsuario(@PathVariable Long idUsuario) {
        return ResponseEntity.ok(metaService.obtenerPorUsuario(idUsuario));
    }

    /**
     * Elimina una meta existente.
     *
     * @param id identificador de la meta a eliminar
     * @return respuesta sin contenido (HTTP 204)
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar meta")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        metaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Actualiza la información de una meta existente.
     *
     * @param id identificador de la meta
     * @param meta nuevos datos de la meta
     * @return meta actualizada
     */
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar meta")
    public ResponseEntity<Meta> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody Meta meta) {

        return ResponseEntity.ok(metaService.actualizar(id, meta));
    }
}
