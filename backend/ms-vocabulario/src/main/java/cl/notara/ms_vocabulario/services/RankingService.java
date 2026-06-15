package cl.notara.ms_vocabulario.services;

import cl.notara.ms_vocabulario.models.Partida;
import cl.notara.ms_vocabulario.models.Ranking;
import cl.notara.ms_vocabulario.repositories.RankingRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Servicio encargado de gestionar la lógica de negocio relacionada
 * con los rankings del módulo de vocabulario.
 *
 * <p>
 * Administra la actualización de estadísticas de usuarios luego de
 * finalizar partidas, manteniendo información como puntuación acumulada,
 * cantidad de partidas jugadas, palabras correctas y mejores rachas.
 * </p>
 *
 * <p>
 * Permite manejar rankings generales y rankings filtrados por categoría,
 * además de consultar estadísticas individuales de usuarios.
 * </p>
 *
 * @author Notara
 * @version 1.0
 */
@Service
public class RankingService {

    /**
     * Repositorio encargado de la persistencia y consulta de rankings.
     */
    private final RankingRepository rankingRepo;


    /**
     * Constructor que inyecta el repositorio de rankings.
     *
     * @param rankingRepo repositorio de acceso a datos del ranking
     */
    public RankingService(RankingRepository rankingRepo) {
        this.rankingRepo = rankingRepo;
    }


    /**
     * Actualiza el ranking de un usuario después de completar una partida.
     *
     * <p>
     * Actualiza dos registros:
     * </p>
     *
     * <ul>
     *     <li>Ranking correspondiente a la categoría de la partida.</li>
     *     <li>Ranking global del usuario.</li>
     * </ul>
     *
     * @param partida partida finalizada con los resultados obtenidos
     */
    @Caching(evict = {
            @CacheEvict(value = "ranking-global", allEntries = true),
            @CacheEvict(value = "ranking-categoria", allEntries = true),
            @CacheEvict(value = "estadisticas-usuario", key = "#partida.idUsuario")
    })
    @Transactional
    public void actualizarRanking(Partida partida) {

        actualizarEntrada(
                partida,
                partida.getCategoria()
        );

        // Actualización del ranking global
        actualizarEntrada(
                partida,
                null
        );
    }


    /**
     * Actualiza o crea una entrada específica del ranking.
     *
     * <p>
     * Busca un ranking existente para el usuario y categoría indicada.
     * Si no existe, crea uno nuevo y posteriormente actualiza sus
     * estadísticas acumuladas.
     * </p>
     *
     * @param partida partida con los resultados obtenidos
     * @param categoria categoría asociada al ranking.
     *                  Si es null representa el ranking global
     */
    private void actualizarEntrada(
            Partida partida,
            cl.notara.ms_vocabulario.models.Categoria categoria
    ) {

        Ranking ranking =
                categoria == null
                ?
                rankingRepo
                    .findByIdUsuarioAndCategoriaIsNull(
                            partida.getIdUsuario()
                    )
                    .orElseGet(
                            () -> crearNuevo(partida, null)
                    )
                :
                rankingRepo
                    .findByIdUsuarioAndCategoria(
                            partida.getIdUsuario(),
                            categoria
                    )
                    .orElseGet(
                            () -> crearNuevo(partida, categoria)
                    );


        ranking.setNombreUsuario(
                partida.getNombreUsuario()
        );


        ranking.setTotalPartidas(
                ranking.getTotalPartidas() + 1
        );


        ranking.setPuntuacionTotal(
                ranking.getPuntuacionTotal()
                        + partida.getPuntuacion()
        );


        ranking.setTotalPalabrasCorrectas(
                ranking.getTotalPalabrasCorrectas()
                        + partida.getPalabrasCorrectas()
        );


        ranking.setTotalPalabras(
                ranking.getTotalPalabras()
                        + partida.getTotalPreguntas()
        );


        // Actualiza la mejor puntuación obtenida
        if (partida.getPuntuacion()
                > ranking.getMejorPuntuacion()) {

            ranking.setMejorPuntuacion(
                    partida.getPuntuacion()
            );
        }


        // Actualiza la mejor racha conseguida
        if (partida.getMejorRacha()
                > ranking.getMejorRacha()) {

            ranking.setMejorRacha(
                    partida.getMejorRacha()
            );
        }


        rankingRepo.save(ranking);
    }


    /**
     * Obtiene el ranking global de usuarios.
     *
     * <p>
     * Retorna los 10 usuarios con mejor puntuación global.
     * </p>
     *
     * @return lista ordenada de rankings globales
     */
    @Cacheable(value = "ranking-global")
    public List<Ranking> rankingGlobal() {

        return rankingRepo
                .findTop10ByCategoriaIsNullOrderByMejorPuntuacionDesc();
    }


    /**
     * Obtiene el ranking filtrado por categoría.
     *
     * @param categoria categoría utilizada como filtro
     * @return lista de mejores usuarios dentro de la categoría
     */
    @Cacheable(value = "ranking-categoria", key = "#categoria.name()")
    public List<Ranking> rankingPorCategoria(
            cl.notara.ms_vocabulario.models.Categoria categoria
    ) {

        return rankingRepo
                .findTop10ByCategoriaOrderByMejorPuntuacionDesc(
                        categoria
                );
    }


    /**
     * Obtiene las estadísticas de ranking de un usuario.
     *
     * @param idUsuario identificador del usuario
     * @return lista de rankings asociados al usuario
     */
    @Cacheable(value = "estadisticas-usuario", key = "#idUsuario")
    public List<Ranking> estadisticasUsuario(
            Long idUsuario
    ) {

        return rankingRepo.findByIdUsuario(
                idUsuario
        );
    }


    /**
     * Crea una nueva entrada de ranking para un usuario.
     *
     * <p>
     * Inicializa los datos básicos del ranking cuando el usuario
     * participa por primera vez en una categoría o ranking global.
     * </p>
     *
     * @param partida partida utilizada para obtener datos del usuario
     * @param categoria categoría asociada al ranking
     * @return nuevo objeto Ranking preparado para guardar
     */
    private Ranking crearNuevo(
            Partida partida,
            cl.notara.ms_vocabulario.models.Categoria categoria
    ) {

        Ranking ranking = new Ranking();

        ranking.setIdUsuario(
                partida.getIdUsuario()
        );

        ranking.setNombreUsuario(
                partida.getNombreUsuario()
        );

        ranking.setCategoria(
                categoria
        );

        return ranking;
    }
}