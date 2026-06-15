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

@Service
public class PartidaService {

    private final PartidaRepository partidaRepo;
    private final PreguntaPartidaRepository preguntaRepo;
    private final PalabraRepository palabraRepo;
    private final RankingService rankingService;

    public PartidaService(PartidaRepository partidaRepo,
                          PreguntaPartidaRepository preguntaRepo,
                          PalabraRepository palabraRepo,
                          RankingService rankingService) {
        this.partidaRepo   = partidaRepo;
        this.preguntaRepo  = preguntaRepo;
        this.palabraRepo   = palabraRepo;
        this.rankingService = rankingService;
    }

    @Transactional
    public PreguntaDTO iniciar(IniciarPartidaRequest req) {
        long disponibles = palabraRepo.countByCategoriaAndActivaTrue(req.getCategoria());
        if (disponibles < req.getTotalPreguntas()) {
            throw new IllegalStateException(
                "No hay suficientes palabras en la categoría " + req.getCategoria() +
                ". Disponibles: " + disponibles + ", solicitadas: " + req.getTotalPreguntas());
        }

        Partida partida = new Partida();
        partida.setIdUsuario(req.getIdUsuario());
        partida.setNombreUsuario(req.getNombreUsuario());
        partida.setCategoria(req.getCategoria());
        partida.setTotalPreguntas(req.getTotalPreguntas());
        partida.setTiempoMaximoSegundos(req.getTiempoMaximoSegundos());
        partida = partidaRepo.save(partida);

        List<Palabra> palabras = palabraRepo.findRandomByCategoria(
                req.getCategoria(), PageRequest.of(0, req.getTotalPreguntas()));

        for (int i = 0; i < palabras.size(); i++) {
            PreguntaPartida pp = new PreguntaPartida();
            pp.setPartida(partida);
            pp.setPalabra(palabras.get(i));
            pp.setOrden(i);
            preguntaRepo.save(pp);
        }

        return entregarPregunta(partida, 0);
    }

    @Transactional
    public PreguntaDTO obtenerPreguntaActual(Long idPartida) {
        Partida partida = obtenerPartidaActiva(idPartida);
        PreguntaPartida pp = obtenerPreguntaEnCurso(partida);
        return mapToPreguntaDTO(pp, partida);
    }

    @Transactional
    public RespuestaDTO responder(Long idPartida, ResponderRequest req) {
        Partida partida = obtenerPartidaActiva(idPartida);
        PreguntaPartida pp = obtenerPreguntaEnCurso(partida);

        LocalDateTime ahora = LocalDateTime.now();
        long tiempoMs = Duration.between(pp.getFechaEntregada(), ahora).toMillis();
        long limiteMs = (long) partida.getTiempoMaximoSegundos() * 1000;

        boolean tiempoAgotado = tiempoMs > limiteMs;
        String respuestaUsuario = req.getRespuesta().trim();
        String respuestaCorrecta = pp.getPalabra().getPalabra();
        boolean esCorrecta = !tiempoAgotado &&
                             respuestaCorrecta.equalsIgnoreCase(respuestaUsuario);

        int puntos = esCorrecta ? calcularPuntos(pp.getPalabra().getDificultad(), tiempoMs, limiteMs, partida.getRachaActual()) : 0;

        pp.setRespuestaUsuario(respuestaUsuario);
        pp.setEsCorrecta(esCorrecta);
        pp.setTiempoRespuestaMs(tiempoMs);
        pp.setPuntosObtenidos(puntos);
        pp.setEstado(tiempoAgotado ? EstadoPregunta.TIEMPO_AGOTADO : EstadoPregunta.RESPONDIDA);
        pp.setFechaRespondida(ahora);
        preguntaRepo.save(pp);

        partida.setPuntuacion(partida.getPuntuacion() + puntos);

        if (esCorrecta) {
            partida.setPalabrasCorrectas(partida.getPalabrasCorrectas() + 1);
            int nuevaRacha = partida.getRachaActual() + 1;
            partida.setRachaActual(nuevaRacha);
            if (nuevaRacha > partida.getMejorRacha()) partida.setMejorRacha(nuevaRacha);
        } else {
            partida.setRachaActual(0);
        }

        int siguienteIndex = partida.getPreguntaActualIndex() + 1;
        partida.setPreguntaActualIndex(siguienteIndex);

        RespuestaDTO resp = new RespuestaDTO();
        resp.setEsCorrecta(esCorrecta);
        resp.setTiempoAgotado(tiempoAgotado);
        resp.setRespuestaCorrecta(respuestaCorrecta);
        resp.setRespuestaUsuario(respuestaUsuario);
        resp.setPuntosObtenidos(puntos);
        resp.setPuntuacionActual(partida.getPuntuacion());
        resp.setRachaActual(partida.getRachaActual());
        resp.setPalabrasCorrectas(partida.getPalabrasCorrectas());
        resp.setNumeroPregunta(siguienteIndex);
        resp.setTotalPreguntas(partida.getTotalPreguntas());

        if (siguienteIndex >= partida.getTotalPreguntas()) {
            partida.setEstado(EstadoPartida.FINALIZADA);
            partida.setFechaFin(LocalDateTime.now());
            partidaRepo.save(partida);
            resp.setGameOver(true);
            resp.setResumen(construirResumen(partida));
            rankingService.actualizarRanking(partida);
        } else {
            partidaRepo.save(partida);
            resp.setGameOver(false);
            resp.setSiguientePregunta(entregarPregunta(partida, siguienteIndex));
        }

        return resp;
    }

    @Transactional
    public void abandonar(Long idPartida) {
        Partida partida = obtenerPartidaActiva(idPartida);
        partida.setEstado(EstadoPartida.ABANDONADA);
        partida.setFechaFin(LocalDateTime.now());
        partidaRepo.save(partida);
    }

    public Partida obtener(Long id) {
        return partidaRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Partida no encontrada con id: " + id));
    }

    public List<Partida> historialUsuario(Long idUsuario) {
        return partidaRepo.findByIdUsuarioOrderByFechaInicioDesc(idUsuario);
    }

    // ─── Privados ────────────────────────────────────────────────────────────

    private PreguntaDTO entregarPregunta(Partida partida, int index) {
        PreguntaPartida pp = preguntaRepo.findByPartidaIdAndOrden(partida.getId(), index)
                .orElseThrow(() -> new ResourceNotFoundException("Pregunta no encontrada"));
        pp.setEstado(EstadoPregunta.ENTREGADA);
        pp.setFechaEntregada(LocalDateTime.now());
        preguntaRepo.save(pp);
        return mapToPreguntaDTO(pp, partida);
    }

    private Partida obtenerPartidaActiva(Long idPartida) {
        Partida partida = obtener(idPartida);
        if (partida.getEstado() != EstadoPartida.EN_CURSO) {
            throw new IllegalStateException("La partida no está en curso (estado: " + partida.getEstado() + ")");
        }
        return partida;
    }

    private PreguntaPartida obtenerPreguntaEnCurso(Partida partida) {
        return preguntaRepo.findByPartidaIdAndEstado(partida.getId(), EstadoPregunta.ENTREGADA)
                .orElseThrow(() -> new IllegalStateException("No hay pregunta activa en esta partida"));
    }

    private PreguntaDTO mapToPreguntaDTO(PreguntaPartida pp, Partida partida) {
        PreguntaDTO dto = new PreguntaDTO();
        dto.setIdPregunta(pp.getId());
        dto.setIdPartida(partida.getId());
        dto.setDefinicion(pp.getPalabra().getDefinicion());
        dto.setPista(pp.getPalabra().getPista());
        dto.setCategoria(pp.getPalabra().getCategoria());
        dto.setDificultad(pp.getPalabra().getDificultad());
        dto.setNumeroPregunta(pp.getOrden() + 1);
        dto.setTotalPreguntas(partida.getTotalPreguntas());
        dto.setPuntuacionActual(partida.getPuntuacion());
        dto.setRachaActual(partida.getRachaActual());
        dto.setTiempoMaximoSegundos(partida.getTiempoMaximoSegundos());
        dto.setFechaEntregada(pp.getFechaEntregada());
        return dto;
    }

    private int calcularPuntos(Dificultad dificultad, long tiempoMs, long limiteMs, int racha) {
        int base = switch (dificultad) {
            case FACIL   -> 10;
            case MEDIO   -> 20;
            case DIFICIL -> 30;
        };

        // Bonus tiempo: hasta 50% del puntaje base por responder rápido
        double tiempoRatio = Math.max(0.0, 1.0 - (double) tiempoMs / limiteMs);
        int bonusTiempo = (int) (base * 0.5 * tiempoRatio);

        // Bonus racha: +25% por cada 3 aciertos consecutivos
        int bonusRacha = (racha / 3) * (int) (base * 0.25);

        return base + bonusTiempo + bonusRacha;
    }

    private PartidaResumenDTO construirResumen(Partida partida) {
        double precision = partida.getTotalPreguntas() == 0 ? 0
                : (double) partida.getPalabrasCorrectas() / partida.getTotalPreguntas() * 100;

        String calificacion;
        if (precision >= 90)      calificacion = "Excelente 🏆";
        else if (precision >= 70) calificacion = "Muy bien 🌟";
        else if (precision >= 50) calificacion = "Bien 👍";
        else if (precision >= 30) calificacion = "Regular 📚";
        else                      calificacion = "Sigue practicando 💪";

        PartidaResumenDTO res = new PartidaResumenDTO();
        res.setIdPartida(partida.getId());
        res.setIdUsuario(partida.getIdUsuario());
        res.setNombreUsuario(partida.getNombreUsuario());
        res.setCategoria(partida.getCategoria());
        res.setPuntuacionFinal(partida.getPuntuacion());
        res.setPalabrasCorrectas(partida.getPalabrasCorrectas());
        res.setTotalPreguntas(partida.getTotalPreguntas());
        res.setPrecision(Math.round(precision * 10.0) / 10.0);
        res.setMejorRacha(partida.getMejorRacha());
        res.setCalificacion(calificacion);
        return res;
    }
}
