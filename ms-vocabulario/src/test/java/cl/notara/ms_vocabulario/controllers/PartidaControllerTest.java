package cl.notara.ms_vocabulario.controllers;

import cl.notara.ms_vocabulario.dto.*;
import cl.notara.ms_vocabulario.exceptions.GlobalExceptionHandler;
import cl.notara.ms_vocabulario.exceptions.ResourceNotFoundException;
import cl.notara.ms_vocabulario.models.*;
import cl.notara.ms_vocabulario.services.PartidaService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class PartidaControllerTest {

    @Mock
    private PartidaService service;

    @InjectMocks
    private PartidaController controller;

    private MockMvc mockMvc;
    private ObjectMapper mapper;
    private PreguntaDTO preguntaDTO;
    private RespuestaDTO respuestaDTO;
    private Partida partida;

    @BeforeEach
    void setUp() {
        mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(mapper))
                .build();

        preguntaDTO = new PreguntaDTO();
        preguntaDTO.setIdPregunta(1L);
        preguntaDTO.setIdPartida(1L);
        preguntaDTO.setDefinicion("Una vivienda donde vive la gente");
        preguntaDTO.setPista("Hogar");
        preguntaDTO.setCategoria(Categoria.BASICO);
        preguntaDTO.setDificultad(Dificultad.FACIL);
        preguntaDTO.setNumeroPregunta(1);
        preguntaDTO.setTotalPreguntas(5);
        preguntaDTO.setTiempoMaximoSegundos(30);
        preguntaDTO.setFechaEntregada(LocalDateTime.now());

        respuestaDTO = new RespuestaDTO();
        respuestaDTO.setEsCorrecta(true);
        respuestaDTO.setPuntosObtenidos(15);
        respuestaDTO.setPuntuacionActual(15);
        respuestaDTO.setRachaActual(1);
        respuestaDTO.setNumeroPregunta(2);
        respuestaDTO.setTotalPreguntas(5);
        respuestaDTO.setGameOver(false);
        respuestaDTO.setSiguientePregunta(preguntaDTO);

        partida = new Partida();
        partida.setId(1L);
        partida.setIdUsuario(1L);
        partida.setNombreUsuario("Test User");
        partida.setCategoria(Categoria.BASICO);
        partida.setEstado(EstadoPartida.EN_CURSO);
    }

    @Test
    @DisplayName("POST /vocabulario/partidas → 201 con primera pregunta")
    void iniciar_retorna201() throws Exception {
        IniciarPartidaRequest req = new IniciarPartidaRequest();
        req.setIdUsuario(1L);
        req.setNombreUsuario("Test User");
        req.setCategoria(Categoria.BASICO);
        req.setTotalPreguntas(5);
        req.setTiempoMaximoSegundos(30);

        when(service.iniciar(any(IniciarPartidaRequest.class))).thenReturn(preguntaDTO);

        mockMvc.perform(post("/vocabulario/partidas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idPartida").value(1))
                .andExpect(jsonPath("$.numeroPregunta").value(1));
    }

    @Test
    @DisplayName("POST /vocabulario/partidas sin palabras suficientes → 409")
    void iniciar_sinPalabrasSuficientes_retorna409() throws Exception {
        IniciarPartidaRequest req = new IniciarPartidaRequest();
        req.setIdUsuario(1L);
        req.setNombreUsuario("Test User");
        req.setCategoria(Categoria.BASICO);
        req.setTotalPreguntas(5);
        req.setTiempoMaximoSegundos(30);

        when(service.iniciar(any())).thenThrow(new IllegalStateException("No hay suficientes palabras"));

        mockMvc.perform(post("/vocabulario/partidas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("GET /vocabulario/partidas/{id}/pregunta → 200 con pregunta actual")
    void preguntaActual_retorna200() throws Exception {
        when(service.obtenerPreguntaActual(1L)).thenReturn(preguntaDTO);

        mockMvc.perform(get("/vocabulario/partidas/1/pregunta"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.definicion").value("Una vivienda donde vive la gente"))
                .andExpect(jsonPath("$.tiempoMaximoSegundos").value(30));
    }

    @Test
    @DisplayName("GET /vocabulario/partidas/{id}/pregunta partida no activa → 409")
    void preguntaActual_partidaNoActiva_retorna409() throws Exception {
        when(service.obtenerPreguntaActual(1L))
                .thenThrow(new IllegalStateException("La partida no está en curso"));

        mockMvc.perform(get("/vocabulario/partidas/1/pregunta"))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("POST /vocabulario/partidas/{id}/responder respuesta correcta → 200")
    void responder_respuestaCorrecta_retorna200() throws Exception {
        ResponderRequest req = new ResponderRequest();
        req.setRespuesta("house");

        when(service.responder(eq(1L), any(ResponderRequest.class))).thenReturn(respuestaDTO);

        mockMvc.perform(post("/vocabulario/partidas/1/responder")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.esCorrecta").value(true))
                .andExpect(jsonPath("$.puntosObtenidos").value(15))
                .andExpect(jsonPath("$.gameOver").value(false));
    }

    @Test
    @DisplayName("POST /vocabulario/partidas/{id}/responder game over → 200 con resumen")
    void responder_gameOver_retorna200ConResumen() throws Exception {
        ResponderRequest req = new ResponderRequest();
        req.setRespuesta("house");

        RespuestaDTO gameOverResp = new RespuestaDTO();
        gameOverResp.setGameOver(true);
        gameOverResp.setEsCorrecta(true);
        PartidaResumenDTO resumen = new PartidaResumenDTO();
        resumen.setCalificacion("Excelente 🏆");
        resumen.setPuntuacionFinal(150);
        gameOverResp.setResumen(resumen);

        when(service.responder(eq(1L), any())).thenReturn(gameOverResp);

        mockMvc.perform(post("/vocabulario/partidas/1/responder")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gameOver").value(true))
                .andExpect(jsonPath("$.resumen.calificacion").value("Excelente 🏆"));
    }

    @Test
    @DisplayName("PUT /vocabulario/partidas/{id}/abandonar → 204")
    void abandonar_retorna204() throws Exception {
        doNothing().when(service).abandonar(1L);

        mockMvc.perform(put("/vocabulario/partidas/1/abandonar"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("PUT /vocabulario/partidas/{id}/abandonar partida no activa → 409")
    void abandonar_partidaNoActiva_retorna409() throws Exception {
        doThrow(new IllegalStateException("La partida no está en curso")).when(service).abandonar(1L);

        mockMvc.perform(put("/vocabulario/partidas/1/abandonar"))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("GET /vocabulario/partidas/{id} → 200 con partida")
    void obtener_retorna200() throws Exception {
        when(service.obtener(1L)).thenReturn(partida);

        mockMvc.perform(get("/vocabulario/partidas/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.categoria").value("BASICO"));
    }

    @Test
    @DisplayName("GET /vocabulario/partidas/{id} no encontrada → 404")
    void obtener_noExistente_retorna404() throws Exception {
        when(service.obtener(99L)).thenThrow(new ResourceNotFoundException("Partida no encontrada con id: 99"));

        mockMvc.perform(get("/vocabulario/partidas/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /vocabulario/partidas/usuario/{idUsuario} → 200 con historial")
    void historial_retorna200() throws Exception {
        when(service.historialUsuario(1L)).thenReturn(List.of(partida));

        mockMvc.perform(get("/vocabulario/partidas/usuario/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].idUsuario").value(1));
    }

    @Test
    @DisplayName("GET /vocabulario/partidas/usuario/{idUsuario} sin partidas → 200 vacío")
    void historial_sinPartidas_retorna200Vacio() throws Exception {
        when(service.historialUsuario(99L)).thenReturn(List.of());

        mockMvc.perform(get("/vocabulario/partidas/usuario/99"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
}
