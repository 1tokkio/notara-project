package cl.notara.ms_pagos_subscripciones.controllers;

import cl.notara.ms_pagos_subscripciones.exceptions.GlobalExceptionHandler;
import cl.notara.ms_pagos_subscripciones.exceptions.ResourceNotFoundException;
import cl.notara.ms_pagos_subscripciones.models.EstadoSuscripcion;
import cl.notara.ms_pagos_subscripciones.models.Plan;
import cl.notara.ms_pagos_subscripciones.models.Suscripcion;
import cl.notara.ms_pagos_subscripciones.services.SuscripcionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SuscripcionController.class)
@Import(GlobalExceptionHandler.class)
class SuscripcionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SuscripcionService service;

    private ObjectMapper objectMapper;
    private Suscripcion suscripcion;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        suscripcion = new Suscripcion();
        suscripcion.setId(1L);
        suscripcion.setIdUsuario(10L);
        suscripcion.setEmailUsuario("test@example.com");
        suscripcion.setNombreUsuario("Juan Perez");
        suscripcion.setPlan(Plan.BASICO);
        suscripcion.setEstado(EstadoSuscripcion.ACTIVA);
        suscripcion.setFechaInicio(LocalDate.of(2025, 1, 1));
        suscripcion.setFechaFin(LocalDate.of(2025, 12, 31));
        suscripcion.setMonto(9990.0);
    }

    @Test
    void listar_retorna200ConLista() throws Exception {
        when(service.listar()).thenReturn(Arrays.asList(suscripcion));

        mockMvc.perform(get("/suscripciones"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].emailUsuario").value("test@example.com"));
    }

    @Test
    void listar_retorna200ConListaVacia() throws Exception {
        when(service.listar()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/suscripciones"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void obtener_cuandoExiste_retorna200() throws Exception {
        when(service.obtener(1L)).thenReturn(suscripcion);

        mockMvc.perform(get("/suscripciones/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.nombreUsuario").value("Juan Perez"));
    }

    @Test
    void obtener_cuandoNoExiste_retorna404() throws Exception {
        when(service.obtener(99L))
                .thenThrow(new ResourceNotFoundException("Suscripción no encontrada con id: 99"));

        mockMvc.perform(get("/suscripciones/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void listarPorUsuario_retorna200ConSuscripciones() throws Exception {
        when(service.listarPorUsuario(10L)).thenReturn(Arrays.asList(suscripcion));

        mockMvc.perform(get("/suscripciones/usuario/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].idUsuario").value(10L));
    }

    @Test
    void listarPorUsuario_usuarioSinSuscripciones_retornaListaVacia() throws Exception {
        when(service.listarPorUsuario(99L)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/suscripciones/usuario/99"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void crear_conDatosValidos_retorna201() throws Exception {
        when(service.crear(any(Suscripcion.class))).thenReturn(suscripcion);

        mockMvc.perform(post("/suscripciones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(suscripcion)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void crear_conCamposRequeridos_retorna400() throws Exception {
        Suscripcion invalida = new Suscripcion();

        mockMvc.perform(post("/suscripciones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalida)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores").exists());
    }

    @Test
    void crear_conSuscripcionActivaExistente_retorna409() throws Exception {
        when(service.crear(any(Suscripcion.class)))
                .thenThrow(new IllegalStateException("El usuario ya tiene una suscripción activa"));

        mockMvc.perform(post("/suscripciones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(suscripcion)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void cancelar_retorna200ConEstadoCancelado() throws Exception {
        suscripcion.setEstado(EstadoSuscripcion.CANCELADA);
        when(service.cancelar(1L)).thenReturn(suscripcion);

        mockMvc.perform(put("/suscripciones/1/cancelar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("CANCELADA"));
    }

    @Test
    void cancelar_yaEstabaCancelada_retorna409() throws Exception {
        when(service.cancelar(1L))
                .thenThrow(new IllegalStateException("La suscripción ya está cancelada"));

        mockMvc.perform(put("/suscripciones/1/cancelar"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void cancelar_noExiste_retorna404() throws Exception {
        when(service.cancelar(99L))
                .thenThrow(new ResourceNotFoundException("Suscripción no encontrada con id: 99"));

        mockMvc.perform(put("/suscripciones/99/cancelar"))
                .andExpect(status().isNotFound());
    }

    @Test
    void renovar_retorna200ConNuevaFecha() throws Exception {
        LocalDate nuevaFecha = LocalDate.of(2026, 6, 1);
        suscripcion.setFechaFin(nuevaFecha);
        when(service.renovar(eq(1L), any(LocalDate.class))).thenReturn(suscripcion);

        String body = objectMapper.writeValueAsString(Map.of("fechaFin", "2026-06-01"));

        mockMvc.perform(put("/suscripciones/1/renovar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void renovar_suscripcionCancelada_retorna409() throws Exception {
        when(service.renovar(eq(1L), any(LocalDate.class)))
                .thenThrow(new IllegalStateException("No se puede renovar una suscripción cancelada"));

        String body = objectMapper.writeValueAsString(Map.of("fechaFin", "2026-06-01"));

        mockMvc.perform(put("/suscripciones/1/renovar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict());
    }
}
