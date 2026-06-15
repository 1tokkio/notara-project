package cl.notara.ms_vocabulario.controllers;

import cl.notara.ms_vocabulario.exceptions.ResourceNotFoundException;
import cl.notara.ms_vocabulario.models.Categoria;
import cl.notara.ms_vocabulario.models.Palabra;
import cl.notara.ms_vocabulario.repositories.PalabraRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/vocabulario/palabras")
public class PalabraController {

    private final PalabraRepository repo;

    public PalabraController(PalabraRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public ResponseEntity<List<Palabra>> listar() {
        return ResponseEntity.ok(repo.findAll());
    }

    @GetMapping("/categorias")
    public ResponseEntity<Map<String, Long>> resumenCategorias() {
        Map<String, Long> resumen = Arrays.stream(Categoria.values())
                .collect(Collectors.toMap(
                        Enum::name,
                        repo::countByCategoriaAndActivaTrue));
        return ResponseEntity.ok(resumen);
    }

    @GetMapping("/categoria/{categoria}")
    public ResponseEntity<List<Palabra>> porCategoria(@PathVariable Categoria categoria) {
        return ResponseEntity.ok(repo.findByCategoriaAndActivaTrue(categoria));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Palabra> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Palabra no encontrada con id: " + id)));
    }

    @PostMapping
    public ResponseEntity<Palabra> crear(@Valid @RequestBody Palabra palabra) {
        return ResponseEntity.status(HttpStatus.CREATED).body(repo.save(palabra));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Palabra> actualizar(@PathVariable Long id, @Valid @RequestBody Palabra datos) {
        Palabra existente = repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Palabra no encontrada con id: " + id));
        existente.setPalabra(datos.getPalabra());
        existente.setDefinicion(datos.getDefinicion());
        existente.setPista(datos.getPista());
        existente.setCategoria(datos.getCategoria());
        existente.setDificultad(datos.getDificultad());
        existente.setActiva(datos.isActiva());
        return ResponseEntity.ok(repo.save(existente));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        if (!repo.existsById(id)) throw new ResourceNotFoundException("Palabra no encontrada con id: " + id);
        repo.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
