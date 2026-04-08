package cl.notara.ms_notas_metas.controllers;
import cl.notara.ms_notas_metas.models.Nota;
import cl.notara.ms_notas_metas.services.NotaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notas")
@Tag(name = "Notas", description = "Operaciones relacionadas con notas")
public class NotaController {

    private final NotaService notaService;

    public NotaController(NotaService notaService) {
        this.notaService = notaService;
    }

    @Operation(summary = "Listar todas las notas")
    @GetMapping
    public ResponseEntity<List<Nota>> listar() {
        return ResponseEntity.ok(notaService.listar());
    }

    @Operation(summary = "creador de las notas")
    @PostMapping
    public ResponseEntity<Nota> crear(@Valid @RequestBody Nota nota) {
        return ResponseEntity.ok(notaService.guardar(nota));
    }

    @Operation(summary = "buscador de las notas")
    @GetMapping("/{id}")
    public ResponseEntity<Nota> obtener(@PathVariable Long id) {
        return notaService.obtener(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "eliminador de las notas")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        notaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
