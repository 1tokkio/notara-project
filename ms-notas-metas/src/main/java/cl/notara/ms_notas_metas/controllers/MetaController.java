package cl.notara.ms_notas_metas.controllers;

import cl.notara.ms_notas_metas.models.Meta;
import cl.notara.ms_notas_metas.services.MetaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/metas")
@Tag(name = "Metas", description = "Operaciones relacionadas con metas")
public class MetaController {

    private final MetaService metaService;

    public MetaController(MetaService metaService) {
        this.metaService = metaService;
    }

    @GetMapping
    @Operation(summary = "Listar todas las metas")
    public ResponseEntity<List<Meta>> listar() {
        return ResponseEntity.ok(metaService.listar());
    }

    @PostMapping
    @Operation(summary = "Crear meta")
    public ResponseEntity<Meta> crear(@Valid @RequestBody Meta meta) {
        return ResponseEntity.status(201).body(metaService.guardar(meta));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener meta por ID")
    public ResponseEntity<Meta> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(metaService.obtener(id));
    }

    @GetMapping("/usuario/{idUsuario}")
    @Operation(summary = "Obtener metas por usuario")
    public ResponseEntity<List<Meta>> obtenerPorUsuario(@PathVariable Long idUsuario) {
        return ResponseEntity.ok(metaService.obtenerPorUsuario(idUsuario));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar meta")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        metaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
