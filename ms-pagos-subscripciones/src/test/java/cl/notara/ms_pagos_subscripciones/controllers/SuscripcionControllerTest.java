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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class SuscripcionControllerTest {

    @Mock
    private SuscripcionService service;

    @InjectMocks
    private SuscripcionController controller;

    private MockMvc mockMvc;
    private ObjectMapper mapper;
    private Suscripcion suscripcion;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        suscripcion = new Suscripcion();
        suscripcion.setId(1L);
        suscripcion.setIdUsuario(10L);
        suscripcion.setEmailUsuario("usuario@test.cl");
        suscripcion.setNombreUsuario("Juan Pérez");
        suscripcion.setPlan(Plan.PREMIUM);
        suscripcion.setEstado(EstadoSuscripcion.ACTIVA);
        suscripcion.setFechaInicio(LocalDate.of(2024, 1, 1));
        suscripcion.setFechaFin(LocalDate.of(2024, 2, 1));
        suscripcion.setMonto(9990.0);
    }

    @Test
    @DisplayName("GET /suscripciones → 200 con lista")
    void listar_retorna200ConLista() throws Exception {
        when(service.listar()).thenReturn(List.of(suscripcion));

        mockMvc.perform(get("/suscripciones"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].plan").value("PREMIUM"));
    }

    @Test
    @DisplayName("GET /suscripciones → 200 con lista vacía")
    void listar_retorna200ListaVacia() throws Exception {
        when(service.listar()).thenReturn(List.of());

        mockMvc.perform(get("/suscripciones"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("GET /suscripciones/{id} existente → 200")
    void obtener_existente_retorna200() throws Exception {
        when(service.obtener(1L)).thenReturn(suscripcion);

        mockMvc.perform(get("/suscripciones/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idUsuario").value(10))
                .andExpect(jsonPath("$.emailUsuario").value("usuario@test.cl"));
    }

    @Test
    @DisplayName("GET /suscripciones/{id} no encontrado → 404")
    void obtener_noExistente_retorna404() throws Exception {
        when(service.obtener(99L)).thenThrow(new ResourceNotFoundException("no encontrada con id: 99"));

        mockMvc.perform(get("/suscripciones/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("GET /suscripciones/usuario/{idUsuario} → 200")
    void listarPorUsuario_retorna200() throws Exception {
        when(service.listarPorUsuario(10L)).thenReturn(List.of(suscripcion));

        mockMvc.perform(get("/suscripciones/usuario/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].emailUsuario").value("usuario@test.cl"));
    }

    @Test
    @DisplayName("POST /suscripciones válido → 201")
    void crear_valido_retorna201() throws Exception {
        when(service.crear(any(Suscripcion.class))).thenReturn(suscripcion);

        mockMvc.perform(post("/suscripciones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(suscripcion)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.plan").value("PREMIUM"))
                .andExpect(jsonPath("$.estado").value("ACTIVA"));
    }

    @Test
    @DisplayName("POST /suscripciones con usuario activo → 409")
    void crear_usuarioConSuscripcionActiva_retorna409() throws Exception {
        when(service.crear(any())).thenThrow(new IllegalStateException("ya tiene una suscripción activa"));

        mockMvc.perform(post("/suscripciones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(suscripcion)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    @DisplayName("PUT /suscripciones/{id}/cancelar → 200")
    void cancelar_retorna200() throws Exception {
        suscripcion.setEstado(EstadoSuscripcion.CANCELADA);
        when(service.cancelar(1L)).thenReturn(suscripcion);

        mockMvc.perform(put("/suscripciones/1/cancelar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("CANCELADA"));
    }

    @Test
    @DisplayName("PUT /suscripciones/{id}/cancelar ya cancelada → 409")
    void cancelar_yaCancelada_retorna409() throws Exception {
        when(service.cancelar(1L)).thenThrow(new IllegalStateException("ya está cancelada"));

        mockMvc.perform(put("/suscripciones/1/cancelar"))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("PUT /suscripciones/{id}/cancelar no encontrada → 404")
    void cancelar_noExistente_retorna404() throws Exception {
        when(service.cancelar(99L)).thenThrow(new ResourceNotFoundException("no encontrada con id: 99"));

        mockMvc.perform(put("/suscripciones/99/cancelar"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /suscripciones/{id}/renovar → 200")
    void renovar_retorna200() throws Exception {
        when(service.renovar(eq(1L), any(LocalDate.class))).thenReturn(suscripcion);

        Map<String, String> body = Map.of("fechaFin", "2024-03-01");

        mockMvc.perform(put("/suscripciones/1/renovar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @DisplayName("PUT /suscripciones/{id}/renovar suscripción cancelada → 409")
    void renovar_cancelada_retorna409() throws Exception {
        when(service.renovar(eq(1L), any(LocalDate.class)))
                .thenThrow(new IllegalStateException("No se puede renovar una suscripción cancelada"));

        Map<String, String> body = Map.of("fechaFin", "2024-03-01");

        mockMvc.perform(put("/suscripciones/1/renovar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(body)))
                .andExpect(status().isConflict());
    }
}
