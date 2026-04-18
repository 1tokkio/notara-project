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

    @GetMapping
    @Operation(summary = "Listar todas las notas")
    public ResponseEntity<List<Nota>> listar() {
        return ResponseEntity.ok(notaService.listar());
    }

    @PostMapping
    @Operation(summary = "Crear nota")
    public ResponseEntity<Nota> crear(@Valid @RequestBody Nota nota) {
        return ResponseEntity.status(201).body(notaService.guardar(nota));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener nota por ID")
    public ResponseEntity<Nota> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(notaService.obtener(id));
    }

    @GetMapping("/usuario/{idUsuario}")
    @Operation(summary = "Obtener notas por usuario")
    public ResponseEntity<List<Nota>> obtenerPorUsuario(@PathVariable Long idUsuario) {
        return ResponseEntity.ok(notaService.obtenerPorUsuario(idUsuario));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar nota")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        notaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar nota")
    public ResponseEntity<Nota> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody Nota nota) {

        return ResponseEntity.ok(notaService.actualizar(id, nota));
    }
}
