package cl.notara.ms_vocabulario.services;

import cl.notara.ms_vocabulario.dto.*;
import cl.notara.ms_vocabulario.exceptions.ResourceNotFoundException;
import cl.notara.ms_vocabulario.models.*;
import cl.notara.ms_vocabulario.repositories.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Servicio encargado de gestionar la lógica de negocio relacionada
 * con las partidas de vocabulario.
 *
 * <p>
 * Administra el ciclo completo de una partida, incluyendo:
 * creación, generación de preguntas, validación de respuestas,
 * cálculo de puntuaciones, control de tiempo, rachas y actualización
 * del ranking del usuario.
 * </p>
 *
 * <p>
 * Esta clase actúa como intermediario entre los controladores y los
 * repositorios, aplicando reglas de negocio antes de modificar los
 * datos almacenados.
 * </p>
 *
 * @author Notara
 * @version 1.0
 */
@Service
public class PartidaService {

    /**
     * Repositorio encargado de la persistencia de partidas.
     */
    private final PartidaRepository partidaRepo;

    /**
     * Repositorio encargado de gestionar preguntas asociadas a partidas.
     */
    private final PreguntaPartidaRepository preguntaRepo;

    /**
     * Repositorio encargado de consultar palabras del vocabulario.
     */
    private final PalabraRepository palabraRepo;

    /**
     * Servicio encargado de actualizar estadísticas del ranking.
     */
    private final RankingService rankingService;


    /**
     * Constructor del servicio.
     *
     * @param partidaRepo repositorio de partidas
     * @param preguntaRepo repositorio de preguntas de partida
     * @param palabraRepo repositorio de palabras
     * @param rankingService servicio de ranking
     */
    public PartidaService(
            PartidaRepository partidaRepo,
            PreguntaPartidaRepository preguntaRepo,
            PalabraRepository palabraRepo,
            RankingService rankingService
    ) {
        this.partidaRepo = partidaRepo;
        this.preguntaRepo = preguntaRepo;
        this.palabraRepo = palabraRepo;
        this.rankingService = rankingService;
    }


    /**
     * Inicia una nueva partida de vocabulario.
     *
     * <p>
     * Valida que existan suficientes palabras activas dentro de la
     * categoría seleccionada, crea la partida y genera las preguntas
     * aleatorias correspondientes.
     * </p>
     *
     * @param req información necesaria para crear la partida
     * @return primera pregunta generada
     * @throws IllegalStateException si no existen suficientes palabras
     */
    @Transactional
    public PreguntaDTO iniciar(IniciarPartidaRequest req) {

        long disponibles =
                palabraRepo.countByCategoriaAndActivaTrue(
                        req.getCategoria()
                );

        if (disponibles < req.getTotalPreguntas()) {

            throw new IllegalStateException(
                    "No hay suficientes palabras en la categoría "
                            + req.getCategoria()
            );
        }

        Partida partida = new Partida();

        partida.setIdUsuario(req.getIdUsuario());
        partida.setNombreUsuario(req.getNombreUsuario());
        partida.setCategoria(req.getCategoria());
        partida.setTotalPreguntas(req.getTotalPreguntas());
        partida.setTiempoMaximoSegundos(req.getTiempoMaximoSegundos());

        partida = partidaRepo.save(partida);


        List<Palabra> palabras =
                palabraRepo.findRandomByCategoria(
                        req.getCategoria(),
                        PageRequest.of(
                                0,
                                req.getTotalPreguntas()
                        )
                );


        for (int i = 0; i < palabras.size(); i++) {

            PreguntaPartida pregunta =
                    new PreguntaPartida();

            pregunta.setPartida(partida);
            pregunta.setPalabra(palabras.get(i));
            pregunta.setOrden(i);

            preguntaRepo.save(pregunta);
        }


        return entregarPregunta(partida, 0);
    }


    /**
     * Obtiene la pregunta actual de una partida activa.
     *
     * @param idPartida identificador de la partida
     * @return pregunta actual
     */
    @Transactional
    public PreguntaDTO obtenerPreguntaActual(Long idPartida) {

        Partida partida =
                obtenerPartidaActiva(idPartida);

        PreguntaPartida pregunta =
                obtenerPreguntaEnCurso(partida);

        return mapToPreguntaDTO(
                pregunta,
                partida
        );
    }


    /**
     * Procesa una respuesta enviada por el usuario.
     *
     * <p>
     * Evalúa si la respuesta es correcta, calcula puntos,
     * actualiza estadísticas de la partida y determina si
     * finalizó el juego.
     * </p>
     *
     * @param idPartida identificador de partida
     * @param req respuesta enviada por el usuario
     * @return resultado de la respuesta procesada
     */
    @Transactional
    public RespuestaDTO responder(
            Long idPartida,
            ResponderRequest req
    ) {

        Partida partida =
                obtenerPartidaActiva(idPartida);

        PreguntaPartida pregunta =
                obtenerPreguntaEnCurso(partida);


        LocalDateTime ahora =
                LocalDateTime.now();


        long tiempo =
                Duration.between(
                        pregunta.getFechaEntregada(),
                        ahora
                ).toMillis();


        long limite =
                partida.getTiempoMaximoSegundos()
                        * 1000L;


        boolean tiempoAgotado =
                tiempo > limite;


        String respuestaUsuario =
                req.getRespuesta().trim();


        String respuestaCorrecta =
                pregunta.getPalabra().getPalabra();


        boolean correcta =
                !tiempoAgotado &&
                respuestaCorrecta.equalsIgnoreCase(
                        respuestaUsuario
                );


        int puntos =
                correcta
                ? calcularPuntos(
                        pregunta.getPalabra().getDificultad(),
                        tiempo,
                        limite,
                        partida.getRachaActual()
                )
                : 0;


        pregunta.setRespuestaUsuario(respuestaUsuario);
        pregunta.setEsCorrecta(correcta);
        pregunta.setTiempoRespuestaMs(tiempo);
        pregunta.setPuntosObtenidos(puntos);
        pregunta.setEstado(tiempoAgotado ? EstadoPregunta.TIEMPO_AGOTADO : EstadoPregunta.RESPONDIDA);
        pregunta.setFechaRespondida(ahora);

        preguntaRepo.save(pregunta);


        partida.setPuntuacion(
                partida.getPuntuacion() + puntos
        );


        if (correcta) {

            partida.setPalabrasCorrectas(
                    partida.getPalabrasCorrectas() + 1
            );

            partida.setRachaActual(
                    partida.getRachaActual() + 1
            );

        } else {

            partida.setRachaActual(0);

        }


        if (partida.getRachaActual() > partida.getMejorRacha()) {
            partida.setMejorRacha(partida.getRachaActual());
        }


        int siguiente =
                partida.getPreguntaActualIndex() + 1;


        partida.setPreguntaActualIndex(siguiente);


        RespuestaDTO respuesta =
                new RespuestaDTO();

        respuesta.setEsCorrecta(correcta);
        respuesta.setTiempoAgotado(tiempoAgotado);
        respuesta.setPuntosObtenidos(puntos);
        respuesta.setPuntuacionActual(partida.getPuntuacion());
        respuesta.setRachaActual(partida.getRachaActual());
        respuesta.setPalabrasCorrectas(partida.getPalabrasCorrectas());
        respuesta.setNumeroPregunta(siguiente);
        respuesta.setTotalPreguntas(partida.getTotalPreguntas());
        respuesta.setRespuestaCorrecta(respuestaCorrecta);


        if (siguiente >= partida.getTotalPreguntas()) {

            partida.setEstado(
                    EstadoPartida.FINALIZADA
            );

            partida.setFechaFin(LocalDateTime.now());

            rankingService.actualizarRanking(partida);

            respuesta.setGameOver(true);
            respuesta.setResumen(buildResumen(partida));

        } else {

            respuesta.setGameOver(false);

            respuesta.setSiguientePregunta(
                    entregarPregunta(
                            partida,
                            siguiente
                    )
            );
        }


        partidaRepo.save(partida);


        return respuesta;
    }


    /**
     * Abandona una partida activa.
     *
     * @param idPartida identificador de partida
     */
    @Transactional
    public void abandonar(Long idPartida) {

        Partida partida =
                obtenerPartidaActiva(idPartida);

        partida.setEstado(
                EstadoPartida.ABANDONADA
        );

        partida.setFechaFin(
                LocalDateTime.now()
        );

        partidaRepo.save(partida);
    }


    /**
     * Obtiene una partida por ID.
     *
     * @param id identificador
     * @return partida encontrada
     */
    public Partida obtener(Long id) {

        return partidaRepo.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Partida no encontrada con id: " + id
                        )
                );
    }


    /**
     * Obtiene historial de partidas de un usuario.
     *
     * @param idUsuario identificador del usuario
     * @return lista de partidas
     */
    public List<Partida> historialUsuario(Long idUsuario) {

        return partidaRepo
                .findByIdUsuarioOrderByFechaInicioDesc(
                        idUsuario
                );
    }


    /**
     * Entrega una pregunta específica de una partida.
     */
    private PreguntaDTO entregarPregunta(
            Partida partida,
            int index
    ) {

        PreguntaPartida pregunta =
                preguntaRepo
                .findByPartidaIdAndOrden(
                        partida.getId(),
                        index
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Pregunta no encontrada"
                        )
                );


        pregunta.setEstado(
                EstadoPregunta.ENTREGADA
        );

        pregunta.setFechaEntregada(
                LocalDateTime.now()
        );


        preguntaRepo.save(pregunta);


        return mapToPreguntaDTO(
                pregunta,
                partida
        );
    }


    /**
     * Mapea una PreguntaPartida a su DTO de respuesta.
     */
    private PreguntaDTO mapToPreguntaDTO(
            PreguntaPartida pregunta,
            Partida partida
    ) {

        PreguntaDTO dto = new PreguntaDTO();

        dto.setIdPregunta(pregunta.getId());
        dto.setIdPartida(partida.getId());
        dto.setDefinicion(pregunta.getPalabra().getDefinicion());
        dto.setPista(pregunta.getPalabra().getPista());
        dto.setCategoria(pregunta.getPalabra().getCategoria());
        dto.setDificultad(pregunta.getPalabra().getDificultad());
        dto.setNumeroPregunta(pregunta.getOrden() + 1);
        dto.setTotalPreguntas(partida.getTotalPreguntas());
        dto.setPuntuacionActual(partida.getPuntuacion());
        dto.setRachaActual(partida.getRachaActual());
        dto.setTiempoMaximoSegundos(partida.getTiempoMaximoSegundos());
        dto.setFechaEntregada(pregunta.getFechaEntregada());

        return dto;
    }


    /**
     * Calcula los puntos obtenidos por una respuesta correcta.
     *
     * <p>
     * Aplica puntos base según la dificultad, bonus de velocidad
     * proporcional al tiempo restante, y bonus de racha a partir de 3.
     * </p>
     */
    private int calcularPuntos(
            Dificultad dificultad,
            long tiempo,
            long limite,
            int racha
    ) {

        int base = switch (dificultad) {
            case FACIL  -> 10;
            case MEDIO  -> 20;
            case DIFICIL -> 30;
        };

        double ratio = 1.0 - ((double) tiempo / limite);
        int timeBonus = (int) Math.max(0, base * ratio);
        int puntos = base + timeBonus;

        if (racha >= 3) {
            puntos = (int) (puntos * 1.5);
        }

        return puntos;
    }


    /**
     * Construye el resumen final de una partida terminada.
     */
    private PartidaResumenDTO buildResumen(Partida partida) {

        PartidaResumenDTO resumen = new PartidaResumenDTO();

        resumen.setIdPartida(partida.getId());
        resumen.setIdUsuario(partida.getIdUsuario());
        resumen.setNombreUsuario(partida.getNombreUsuario());
        resumen.setCategoria(partida.getCategoria());
        resumen.setPuntuacionFinal(partida.getPuntuacion());
        resumen.setPalabrasCorrectas(partida.getPalabrasCorrectas());
        resumen.setTotalPreguntas(partida.getTotalPreguntas());
        resumen.setMejorRacha(partida.getMejorRacha());

        double precision = partida.getTotalPreguntas() > 0
                ? (double) partida.getPalabrasCorrectas()
                        / partida.getTotalPreguntas() * 100
                : 0;

        resumen.setPrecision(precision);
        resumen.setCalificacion(calcularCalificacion(precision));

        return resumen;
    }


    /**
     * Determina la calificación textual según la precisión obtenida.
     */
    private String calcularCalificacion(double precision) {

        if (precision >= 90) return "Excelente 🏆";
        if (precision >= 70) return "Muy bien 🌟";
        if (precision >= 50) return "Bien 👍";

        return "Sigue practicando 💪";
    }


    /**
     * Verifica que una partida se encuentre activa.
     */
    private Partida obtenerPartidaActiva(
            Long idPartida
    ) {

        Partida partida =
                obtener(idPartida);


        if (partida.getEstado()
                != EstadoPartida.EN_CURSO) {

            throw new IllegalStateException(
                    "La partida no está en curso"
            );
        }

        return partida;
    }


    /**
     * Obtiene la pregunta actualmente entregada.
     */
    private PreguntaPartida obtenerPreguntaEnCurso(
            Partida partida
    ) {

        return preguntaRepo
                .findByPartidaIdAndEstado(
                        partida.getId(),
                        EstadoPregunta.ENTREGADA
                )
                .orElseThrow(() ->
                        new IllegalStateException(
                                "No existe pregunta activa"
                        )
                );
    }
}
