package cl.notara.ms_vocabulario.services;

import cl.notara.ms_vocabulario.dto.IniciarPartidaRequest;
import cl.notara.ms_vocabulario.dto.PreguntaDTO;
import cl.notara.ms_vocabulario.dto.ResponderRequest;
import cl.notara.ms_vocabulario.dto.RespuestaDTO;
import cl.notara.ms_vocabulario.exceptions.ResourceNotFoundException;
import cl.notara.ms_vocabulario.models.*;
import cl.notara.ms_vocabulario.repositories.PalabraRepository;
import cl.notara.ms_vocabulario.repositories.PartidaRepository;
import cl.notara.ms_vocabulario.repositories.PreguntaPartidaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PartidaServiceTest {

    @Mock
    private PartidaRepository partidaRepo;

    @Mock
    private PreguntaPartidaRepository preguntaRepo;

    @Mock
    private PalabraRepository palabraRepo;

    @Mock
    private RankingService rankingService;

    @InjectMocks
    private PartidaService service;

    private Palabra palabra;
    private Partida partida;
    private PreguntaPartida ppActual;

    @BeforeEach
    void setUp() {
        palabra = new Palabra();
        palabra.setId(1L);
        palabra.setPalabra("house");
        palabra.setDefinicion("Una vivienda donde vive la gente");
        palabra.setPista("Hogar");
        palabra.setCategoria(Categoria.BASICO);
        palabra.setDificultad(Dificultad.FACIL);
        palabra.setActiva(true);

        partida = new Partida();
        partida.setId(1L);
        partida.setIdUsuario(1L);
        partida.setNombreUsuario("Test User");
        partida.setCategoria(Categoria.BASICO);
        partida.setEstado(EstadoPartida.EN_CURSO);
        partida.setTotalPreguntas(5);
        partida.setTiempoMaximoSegundos(30);

        ppActual = new PreguntaPartida();
        ppActual.setId(1L);
        ppActual.setPalabra(palabra);
        ppActual.setOrden(0);
        ppActual.setEstado(EstadoPregunta.ENTREGADA);
        ppActual.setFechaEntregada(LocalDateTime.now().minusSeconds(5));
    }

    // ─── iniciar() ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("iniciar() - palabras suficientes → retorna primera PreguntaDTO")
    void iniciar_suficientesPalabras_retornaPreguntaDTO() {
        IniciarPartidaRequest req = buildRequest(Categoria.BASICO, 5, 30);
        Palabra p2 = buildPalabra(2L, "dog", "Animal doméstico", Categoria.BASICO, Dificultad.FACIL);
        Palabra p3 = buildPalabra(3L, "cat", "Felino doméstico", Categoria.BASICO, Dificultad.MEDIO);
        Palabra p4 = buildPalabra(4L, "sun", "Estrella del sistema solar", Categoria.BASICO, Dificultad.FACIL);
        Palabra p5 = buildPalabra(5L, "moon", "Satélite natural de la Tierra", Categoria.BASICO, Dificultad.MEDIO);

        PreguntaPartida ppPrimera = new PreguntaPartida();
        ppPrimera.setId(10L);
        ppPrimera.setPalabra(palabra);
        ppPrimera.setOrden(0);
        ppPrimera.setEstado(EstadoPregunta.PENDIENTE);

        when(palabraRepo.countByCategoriaAndActivaTrue(Categoria.BASICO)).thenReturn(10L);
        when(partidaRepo.save(any(Partida.class))).thenReturn(partida);
        when(palabraRepo.findRandomByCategoria(eq(Categoria.BASICO), any())).thenReturn(
                List.of(palabra, p2, p3, p4, p5));
        when(preguntaRepo.save(any(PreguntaPartida.class))).thenReturn(ppPrimera);
        when(preguntaRepo.findByPartidaIdAndOrden(1L, 0)).thenReturn(Optional.of(ppPrimera));

        PreguntaDTO resultado = service.iniciar(req);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getIdPartida()).isEqualTo(1L);
        assertThat(resultado.getNumeroPregunta()).isEqualTo(1);
        assertThat(resultado.getTotalPreguntas()).isEqualTo(5);
        verify(partidaRepo).save(any(Partida.class));
        verify(palabraRepo).findRandomByCategoria(eq(Categoria.BASICO), any());
        verify(preguntaRepo, times(6)).save(any(PreguntaPartida.class));
    }

    @Test
    @DisplayName("iniciar() - palabras insuficientes → lanza IllegalStateException")
    void iniciar_insuficientesPalabras_lanzaIllegalState() {
        IniciarPartidaRequest req = buildRequest(Categoria.BASICO, 10, 30);
        when(palabraRepo.countByCategoriaAndActivaTrue(Categoria.BASICO)).thenReturn(5L);

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> service.iniciar(req));
        assertThat(ex.getMessage()).contains("No hay suficientes palabras");
        verify(partidaRepo, never()).save(any());
    }

    // ─── obtenerPreguntaActual() ──────────────────────────────────────────────

    @Test
    @DisplayName("obtenerPreguntaActual() - partida activa → retorna DTO de pregunta")
    void obtenerPreguntaActual_partidaActiva_retornaDTO() {
        when(partidaRepo.findById(1L)).thenReturn(Optional.of(partida));
        when(preguntaRepo.findByPartidaIdAndEstado(1L, EstadoPregunta.ENTREGADA))
                .thenReturn(Optional.of(ppActual));

        PreguntaDTO resultado = service.obtenerPreguntaActual(1L);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getDefinicion()).isEqualTo("Una vivienda donde vive la gente");
        assertThat(resultado.getPista()).isEqualTo("Hogar");
        verify(preguntaRepo).findByPartidaIdAndEstado(1L, EstadoPregunta.ENTREGADA);
    }

    @Test
    @DisplayName("obtenerPreguntaActual() - partida no en curso → lanza IllegalStateException")
    void obtenerPreguntaActual_partidaNoActiva_lanzaIllegalState() {
        partida.setEstado(EstadoPartida.FINALIZADA);
        when(partidaRepo.findById(1L)).thenReturn(Optional.of(partida));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> service.obtenerPreguntaActual(1L));
        assertThat(ex.getMessage()).contains("no está en curso");
    }

    @Test
    @DisplayName("obtenerPreguntaActual() - sin pregunta activa → lanza IllegalStateException")
    void obtenerPreguntaActual_sinPreguntaActiva_lanzaIllegalState() {
        when(partidaRepo.findById(1L)).thenReturn(Optional.of(partida));
        when(preguntaRepo.findByPartidaIdAndEstado(1L, EstadoPregunta.ENTREGADA))
                .thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class,
                () -> service.obtenerPreguntaActual(1L));
    }

    // ─── responder() ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("responder() - respuesta correcta, no última pregunta → actualiza racha")
    void responder_respuestaCorrecta_noUltimaPregunta_actualizaRacha() {
        partida.setPreguntaActualIndex(0);
        ResponderRequest req = new ResponderRequest();
        req.setRespuesta("house");

        PreguntaPartida ppSiguiente = buildPreguntaPartida(2L, buildPalabra(2L, "dog", "Animal doméstico", Categoria.BASICO, Dificultad.MEDIO), 1);

        when(partidaRepo.findById(1L)).thenReturn(Optional.of(partida));
        when(preguntaRepo.findByPartidaIdAndEstado(1L, EstadoPregunta.ENTREGADA))
                .thenReturn(Optional.of(ppActual));
        when(preguntaRepo.save(any())).thenReturn(ppActual);
        when(partidaRepo.save(any())).thenReturn(partida);
        when(preguntaRepo.findByPartidaIdAndOrden(1L, 1)).thenReturn(Optional.of(ppSiguiente));

        RespuestaDTO resultado = service.responder(1L, req);

        assertThat(resultado.isEsCorrecta()).isTrue();
        assertThat(resultado.isTiempoAgotado()).isFalse();
        assertThat(resultado.isGameOver()).isFalse();
        assertThat(resultado.getRachaActual()).isEqualTo(1);
        assertThat(resultado.getPalabrasCorrectas()).isEqualTo(1);
        assertThat(resultado.getPuntosObtenidos()).isGreaterThan(0);
        assertThat(resultado.getSiguientePregunta()).isNotNull();
    }

    @Test
    @DisplayName("responder() - respuesta incorrecta → reinicia racha a 0")
    void responder_respuestaIncorrecta_reiniciaRacha() {
        partida.setPreguntaActualIndex(0);
        partida.setRachaActual(3);
        ResponderRequest req = new ResponderRequest();
        req.setRespuesta("wrong answer");

        PreguntaPartida ppSiguiente = buildPreguntaPartida(2L, buildPalabra(2L, "dog", "Animal", Categoria.BASICO, Dificultad.FACIL), 1);

        when(partidaRepo.findById(1L)).thenReturn(Optional.of(partida));
        when(preguntaRepo.findByPartidaIdAndEstado(1L, EstadoPregunta.ENTREGADA))
                .thenReturn(Optional.of(ppActual));
        when(preguntaRepo.save(any())).thenReturn(ppActual);
        when(partidaRepo.save(any())).thenReturn(partida);
        when(preguntaRepo.findByPartidaIdAndOrden(1L, 1)).thenReturn(Optional.of(ppSiguiente));

        RespuestaDTO resultado = service.responder(1L, req);

        assertThat(resultado.isEsCorrecta()).isFalse();
        assertThat(resultado.getRachaActual()).isEqualTo(0);
        assertThat(resultado.getPuntosObtenidos()).isEqualTo(0);
        assertThat(resultado.isGameOver()).isFalse();
    }

    @Test
    @DisplayName("responder() - tiempo agotado → marca como TIEMPO_AGOTADO, 0 puntos")
    void responder_tiempoAgotado_marcaTiempoAgotado() {
        partida.setPreguntaActualIndex(0);
        ppActual.setFechaEntregada(LocalDateTime.now().minusSeconds(60));
        ResponderRequest req = new ResponderRequest();
        req.setRespuesta("house");

        PreguntaPartida ppSiguiente = buildPreguntaPartida(2L, buildPalabra(2L, "dog", "Animal", Categoria.BASICO, Dificultad.FACIL), 1);

        when(partidaRepo.findById(1L)).thenReturn(Optional.of(partida));
        when(preguntaRepo.findByPartidaIdAndEstado(1L, EstadoPregunta.ENTREGADA))
                .thenReturn(Optional.of(ppActual));
        when(preguntaRepo.save(any())).thenReturn(ppActual);
        when(partidaRepo.save(any())).thenReturn(partida);
        when(preguntaRepo.findByPartidaIdAndOrden(1L, 1)).thenReturn(Optional.of(ppSiguiente));

        RespuestaDTO resultado = service.responder(1L, req);

        assertThat(resultado.isTiempoAgotado()).isTrue();
        assertThat(resultado.isEsCorrecta()).isFalse();
        assertThat(resultado.getPuntosObtenidos()).isEqualTo(0);
    }

    @Test
    @DisplayName("responder() - última pregunta correcta → game over, actualiza ranking")
    void responder_ultimaPreguntaCorrecta_finalizaPartida() {
        partida.setPreguntaActualIndex(4);
        partida.setPalabrasCorrectas(4);
        palabra.setDificultad(Dificultad.MEDIO);
        ResponderRequest req = new ResponderRequest();
        req.setRespuesta("house");

        when(partidaRepo.findById(1L)).thenReturn(Optional.of(partida));
        when(preguntaRepo.findByPartidaIdAndEstado(1L, EstadoPregunta.ENTREGADA))
                .thenReturn(Optional.of(ppActual));
        when(preguntaRepo.save(any())).thenReturn(ppActual);
        when(partidaRepo.save(any())).thenReturn(partida);
        doNothing().when(rankingService).actualizarRanking(any(Partida.class));

        RespuestaDTO resultado = service.responder(1L, req);

        assertThat(resultado.isGameOver()).isTrue();
        assertThat(resultado.getResumen()).isNotNull();
        assertThat(resultado.getResumen().getCalificacion()).contains("Excelente");
        verify(rankingService).actualizarRanking(any(Partida.class));
        verify(partidaRepo, atLeastOnce()).save(any(Partida.class));
    }

    @Test
    @DisplayName("responder() - última pregunta incorrecta → game over con calificación baja")
    void responder_ultimaPreguntaIncorrecta_finalizaPartida() {
        partida.setPreguntaActualIndex(4);
        partida.setPalabrasCorrectas(0);
        ResponderRequest req = new ResponderRequest();
        req.setRespuesta("wrong");

        when(partidaRepo.findById(1L)).thenReturn(Optional.of(partida));
        when(preguntaRepo.findByPartidaIdAndEstado(1L, EstadoPregunta.ENTREGADA))
                .thenReturn(Optional.of(ppActual));
        when(preguntaRepo.save(any())).thenReturn(ppActual);
        when(partidaRepo.save(any())).thenReturn(partida);
        doNothing().when(rankingService).actualizarRanking(any(Partida.class));

        RespuestaDTO resultado = service.responder(1L, req);

        assertThat(resultado.isGameOver()).isTrue();
        assertThat(resultado.getResumen().getCalificacion()).contains("practicando");
        verify(rankingService).actualizarRanking(any(Partida.class));
    }

    @Test
    @DisplayName("responder() - palabra dificultad DIFICIL → calcula puntos base 30")
    void responder_dificultadDificil_calculaPuntosCorrectos() {
        palabra.setDificultad(Dificultad.DIFICIL);
        partida.setPreguntaActualIndex(0);
        ResponderRequest req = new ResponderRequest();
        req.setRespuesta("house");

        PreguntaPartida ppSig = buildPreguntaPartida(2L, buildPalabra(2L, "dog", "Animal", Categoria.BASICO, Dificultad.FACIL), 1);

        when(partidaRepo.findById(1L)).thenReturn(Optional.of(partida));
        when(preguntaRepo.findByPartidaIdAndEstado(1L, EstadoPregunta.ENTREGADA))
                .thenReturn(Optional.of(ppActual));
        when(preguntaRepo.save(any())).thenReturn(ppActual);
        when(partidaRepo.save(any())).thenReturn(partida);
        when(preguntaRepo.findByPartidaIdAndOrden(1L, 1)).thenReturn(Optional.of(ppSig));

        RespuestaDTO resultado = service.responder(1L, req);

        assertThat(resultado.getPuntosObtenidos()).isGreaterThanOrEqualTo(30);
    }

    @Test
    @DisplayName("responder() - racha >= 3 → aplica bonus de racha")
    void responder_rachaAlta_aplicaBonusRacha() {
        partida.setPreguntaActualIndex(0);
        partida.setRachaActual(3);
        ResponderRequest req = new ResponderRequest();
        req.setRespuesta("house");

        PreguntaPartida ppSig = buildPreguntaPartida(2L, buildPalabra(2L, "dog", "Animal", Categoria.BASICO, Dificultad.FACIL), 1);

        when(partidaRepo.findById(1L)).thenReturn(Optional.of(partida));
        when(preguntaRepo.findByPartidaIdAndEstado(1L, EstadoPregunta.ENTREGADA))
                .thenReturn(Optional.of(ppActual));
        when(preguntaRepo.save(any())).thenReturn(ppActual);
        when(partidaRepo.save(any())).thenReturn(partida);
        when(preguntaRepo.findByPartidaIdAndOrden(1L, 1)).thenReturn(Optional.of(ppSig));

        RespuestaDTO resultado = service.responder(1L, req);

        assertThat(resultado.getPuntosObtenidos()).isGreaterThan(10);
    }

    @Test
    @DisplayName("responder() - calificación 'Muy bien' cuando precisión entre 70% y 89%")
    void responder_ultimaPregunta_precision70_calificacionMuyBien() {
        partida.setPreguntaActualIndex(4);
        partida.setPalabrasCorrectas(3);
        partida.setTotalPreguntas(5);
        ResponderRequest req = new ResponderRequest();
        req.setRespuesta("wrong");

        when(partidaRepo.findById(1L)).thenReturn(Optional.of(partida));
        when(preguntaRepo.findByPartidaIdAndEstado(1L, EstadoPregunta.ENTREGADA))
                .thenReturn(Optional.of(ppActual));
        when(preguntaRepo.save(any())).thenReturn(ppActual);
        when(partidaRepo.save(any())).thenReturn(partida);
        doNothing().when(rankingService).actualizarRanking(any());

        RespuestaDTO resultado = service.responder(1L, req);

        assertThat(resultado.getResumen().getCalificacion()).isIn("Muy bien 🌟", "Bien 👍");
    }

    @Test
    @DisplayName("responder() - partida no en curso → lanza IllegalStateException")
    void responder_partidaNoActiva_lanzaIllegalState() {
        partida.setEstado(EstadoPartida.FINALIZADA);
        when(partidaRepo.findById(1L)).thenReturn(Optional.of(partida));

        assertThrows(IllegalStateException.class,
                () -> service.responder(1L, new ResponderRequest() {{ setRespuesta("house"); }}));
    }

    // ─── abandonar() ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("abandonar() - partida activa → marca como ABANDONADA")
    void abandonar_partidaActiva_marcaAbandonada() {
        when(partidaRepo.findById(1L)).thenReturn(Optional.of(partida));
        when(partidaRepo.save(any())).thenReturn(partida);

        service.abandonar(1L);

        assertThat(partida.getEstado()).isEqualTo(EstadoPartida.ABANDONADA);
        assertThat(partida.getFechaFin()).isNotNull();
        verify(partidaRepo).save(partida);
    }

    @Test
    @DisplayName("abandonar() - partida ya finalizada → lanza IllegalStateException")
    void abandonar_partidaNoActiva_lanzaIllegalState() {
        partida.setEstado(EstadoPartida.ABANDONADA);
        when(partidaRepo.findById(1L)).thenReturn(Optional.of(partida));

        assertThrows(IllegalStateException.class, () -> service.abandonar(1L));
    }

    // ─── obtener() ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("obtener() - partida encontrada → retorna objeto Partida")
    void obtener_existente_retornaPartida() {
        when(partidaRepo.findById(1L)).thenReturn(Optional.of(partida));

        Partida resultado = service.obtener(1L);

        assertThat(resultado.getId()).isEqualTo(1L);
        assertThat(resultado.getCategoria()).isEqualTo(Categoria.BASICO);
    }

    @Test
    @DisplayName("obtener() - partida no existe → lanza ResourceNotFoundException")
    void obtener_noExistente_lanzaExcepcion() {
        when(partidaRepo.findById(99L)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> service.obtener(99L));
        assertThat(ex.getMessage()).contains("99");
    }

    // ─── historialUsuario() ──────────────────────────────────────────────────

    @Test
    @DisplayName("historialUsuario() - retorna historial del usuario")
    void historialUsuario_retornaLista() {
        when(partidaRepo.findByIdUsuarioOrderByFechaInicioDesc(1L)).thenReturn(List.of(partida));

        List<Partida> resultado = service.historialUsuario(1L);

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getIdUsuario()).isEqualTo(1L);
        verify(partidaRepo).findByIdUsuarioOrderByFechaInicioDesc(1L);
    }

    @Test
    @DisplayName("historialUsuario() - usuario sin partidas → lista vacía")
    void historialUsuario_sinPartidas_listaVacia() {
        when(partidaRepo.findByIdUsuarioOrderByFechaInicioDesc(99L)).thenReturn(List.of());

        assertThat(service.historialUsuario(99L)).isEmpty();
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private IniciarPartidaRequest buildRequest(Categoria cat, int total, int tiempo) {
        IniciarPartidaRequest req = new IniciarPartidaRequest();
        req.setIdUsuario(1L);
        req.setNombreUsuario("Test User");
        req.setCategoria(cat);
        req.setTotalPreguntas(total);
        req.setTiempoMaximoSegundos(tiempo);
        return req;
    }

    private Palabra buildPalabra(Long id, String palabra, String definicion, Categoria cat, Dificultad dif) {
        Palabra p = new Palabra();
        p.setId(id);
        p.setPalabra(palabra);
        p.setDefinicion(definicion);
        p.setPista("Pista de " + palabra);
        p.setCategoria(cat);
        p.setDificultad(dif);
        p.setActiva(true);
        return p;
    }

    private PreguntaPartida buildPreguntaPartida(Long id, Palabra p, int orden) {
        PreguntaPartida pp = new PreguntaPartida();
        pp.setId(id);
        pp.setPalabra(p);
        pp.setOrden(orden);
        pp.setEstado(EstadoPregunta.PENDIENTE);
        pp.setFechaEntregada(LocalDateTime.now().minusSeconds(2));
        return pp;
    }
}
