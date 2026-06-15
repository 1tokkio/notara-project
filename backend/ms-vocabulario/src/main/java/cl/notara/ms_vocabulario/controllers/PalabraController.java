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

/**
 * Controlador REST encargado de la gestión de palabras del vocabulario.
 *
 * <p>
 * Proporciona endpoints para administrar palabras dentro del sistema,
 * permitiendo realizar operaciones de consulta, creación, actualización
 * y eliminación de registros.
 * </p>
 *
 * <p>
 * También permite consultar información agrupada por categorías y obtener
 * únicamente palabras activas pertenecientes a una categoría específica.
 * </p>
 *
 * <p>
 * La comunicación con la base de datos se realiza mediante
 * {@link PalabraRepository}.
 * </p>
 *
 * @author Notara
 * @version 1.0
 */
@RestController
@RequestMapping("/vocabulario/palabras")
public class PalabraController {

    /**
     * Repositorio encargado de la persistencia de palabras.
     */
    private final PalabraRepository repo;

    /**
     * Constructor que inyecta el repositorio de palabras.
     *
     * @param repo repositorio de acceso a datos
     */
    public PalabraController(PalabraRepository repo) {
        this.repo = repo;
    }

    /**
     * Obtiene todas las palabras registradas.
     *
     * @return lista completa de palabras
     */
    @GetMapping
    public ResponseEntity<List<Palabra>> listar() {
        return ResponseEntity.ok(repo.findAll());
    }

    /**
     * Genera un resumen de la cantidad de palabras activas
     * agrupadas por categoría.
     *
     * <p>
     * Recorre las categorías disponibles en la enumeración
     * {@link Categoria} y obtiene la cantidad de palabras activas
     * asociadas a cada una.
     * </p>
     *
     * @return mapa con categoría y cantidad de palabras activas
     */
    @GetMapping("/categorias")
    public ResponseEntity<Map<String, Long>> resumenCategorias() {

        Map<String, Long> resumen =
                Arrays.stream(Categoria.values())
                        .collect(Collectors.toMap(
                                Enum::name,
                                repo::countByCategoriaAndActivaTrue
                        ));

        return ResponseEntity.ok(resumen);
    }

    /**
     * Obtiene las palabras activas pertenecientes a una categoría.
     *
     * @param categoria categoría de búsqueda
     * @return lista de palabras activas de la categoría indicada
     */
    @GetMapping("/categoria/{categoria}")
    public ResponseEntity<List<Palabra>> porCategoria(
            @PathVariable Categoria categoria
    ) {
        return ResponseEntity.ok(
                repo.findByCategoriaAndActivaTrue(categoria)
        );
    }

    /**
     * Obtiene una palabra mediante su identificador.
     *
     * @param id identificador de la palabra
     * @return palabra encontrada
     * @throws ResourceNotFoundException si la palabra no existe
     */
    @GetMapping("/{id}")
    public ResponseEntity<Palabra> obtener(
            @PathVariable Long id
    ) {

        return ResponseEntity.ok(
                repo.findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Palabra no encontrada con id: " + id
                                )
                        )
        );
    }

    /**
     * Crea una nueva palabra dentro del vocabulario.
     *
     * <p>
     * Los datos recibidos son validados utilizando Bean Validation
     * mediante la anotación {@code @Valid}.
     * </p>
     *
     * @param palabra información de la palabra a crear
     * @return palabra creada
     */
    @PostMapping
    public ResponseEntity<Palabra> crear(
            @Valid @RequestBody Palabra palabra
    ) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(repo.save(palabra));
    }

    /**
     * Actualiza los datos de una palabra existente.
     *
     * @param id identificador de la palabra
     * @param datos nuevos datos de la palabra
     * @return palabra actualizada
     * @throws ResourceNotFoundException si la palabra no existe
     */
    @PutMapping("/{id}")
    public ResponseEntity<Palabra> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody Palabra datos
    ) {

        Palabra existente = repo.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Palabra no encontrada con id: " + id
                        )
                );

        existente.setPalabra(datos.getPalabra());
        existente.setDefinicion(datos.getDefinicion());
        existente.setPista(datos.getPista());
        existente.setCategoria(datos.getCategoria());
        existente.setDificultad(datos.getDificultad());
        existente.setActiva(datos.isActiva());

        return ResponseEntity.ok(
                repo.save(existente)
        );
    }

    /**
     * Elimina una palabra del sistema.
     *
     * @param id identificador de la palabra
     * @return respuesta sin contenido HTTP 204
     * @throws ResourceNotFoundException si la palabra no existe
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(
            @PathVariable Long id
    ) {

        if (!repo.existsById(id)) {

            throw new ResourceNotFoundException(
                    "Palabra no encontrada con id: " + id
            );
        }

        repo.deleteById(id);

        return ResponseEntity.noContent().build();
    }
}
