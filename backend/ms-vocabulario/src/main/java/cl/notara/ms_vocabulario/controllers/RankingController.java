package cl.notara.ms_vocabulario.controllers;

import cl.notara.ms_vocabulario.models.Categoria;
import cl.notara.ms_vocabulario.models.Ranking;
import cl.notara.ms_vocabulario.services.RankingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST encargado de la gestión de rankings del vocabulario.
 *
 * <p>
 * Proporciona endpoints para consultar información estadística y
 * clasificaciones relacionadas con el rendimiento de los usuarios
 * dentro del módulo de vocabulario.
 * </p>
 *
 * <p>
 * Permite obtener rankings generales, rankings filtrados por categoría
 * y estadísticas específicas de un usuario.
 * </p>
 *
 * <p>
 * La lógica de negocio es delegada al servicio {@link RankingService},
 * manteniendo la separación entre la capa de controlador y la capa
 * de servicios.
 * </p>
 *
 * @author Notara
 * @version 1.0
 */
@RestController
@RequestMapping("/vocabulario/ranking")
public class RankingController {

    /**
     * Servicio encargado de procesar la información del ranking.
     */
    private final RankingService service;

    /**
     * Constructor que inyecta el servicio de ranking.
     *
     * @param service servicio de gestión de rankings
     */
    public RankingController(RankingService service) {
        this.service = service;
    }

    /**
     * Obtiene el ranking global del sistema.
     *
     * <p>
     * Retorna la clasificación general considerando todos los usuarios
     * registrados en el módulo de vocabulario.
     * </p>
     *
     * @return lista del ranking global
     */
    @GetMapping
    public ResponseEntity<List<Ranking>> global() {

        return ResponseEntity.ok(
                service.rankingGlobal()
        );
    }

    /**
     * Obtiene el ranking filtrado por una categoría específica.
     *
     * @param categoria categoría de vocabulario utilizada como filtro
     * @return lista de rankings pertenecientes a la categoría indicada
     */
    @GetMapping("/categoria/{categoria}")
    public ResponseEntity<List<Ranking>> porCategoria(
            @PathVariable Categoria categoria
    ) {

        return ResponseEntity.ok(
                service.rankingPorCategoria(categoria)
        );
    }

    /**
     * Obtiene las estadísticas de ranking de un usuario específico.
     *
     * @param idUsuario identificador del usuario
     * @return estadísticas y posición del usuario
     */
    @GetMapping("/usuario/{idUsuario}")
    public ResponseEntity<List<Ranking>> usuario(
            @PathVariable Long idUsuario
    ) {

        return ResponseEntity.ok(
                service.estadisticasUsuario(idUsuario)
        );
    }
}
