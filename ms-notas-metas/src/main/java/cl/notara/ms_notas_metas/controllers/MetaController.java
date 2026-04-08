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

    @Operation(summary = "Listar todas las metas")
    @GetMapping
    public ResponseEntity<List<Meta>> listar() {
        return ResponseEntity.ok(metaService.listar());
    }

    @Operation(summary = "creador de metas")
    @PostMapping
    public ResponseEntity<Meta> crear(@Valid @RequestBody Meta meta) {
        return ResponseEntity.status(201).body(metaService.guardar(meta));
    }

    @Operation(summary = "buscador de metas")
    @GetMapping("/{id}")
    public ResponseEntity<Meta> obtener(@PathVariable Long id) {
        return metaService.obtener(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "eliminador de metas")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        metaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
