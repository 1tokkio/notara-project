package cl.notara.ms_notas_metas.controller;

import cl.notara.ms_notas_metas.controllers.MetaController;
import cl.notara.ms_notas_metas.exceptions.GlobalExceptionHandler;
import cl.notara.ms_notas_metas.exceptions.ResourceNotFoundException;
import cl.notara.ms_notas_metas.models.EstadoMeta;
import cl.notara.ms_notas_metas.models.Meta;
import cl.notara.ms_notas_metas.services.MetaService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MetaController.class)
@Import(GlobalExceptionHandler.class)
class MetaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MetaService metaService;

    @Autowired
    private ObjectMapper objectMapper;

    private Meta metaEjemplo;

    @BeforeEach
    void setUp() {
        metaEjemplo = new Meta();
        metaEjemplo.setId(1L);
        metaEjemplo.setNombre("Aprender 50 palabras en inglés");
        metaEjemplo.setDescripcion("Estudiar vocabulario básico");
        metaEjemplo.setFechaLimite(LocalDate.of(2025, 12, 31));
        metaEjemplo.setCompletada(false);
        metaEjemplo.setIdUsuario(1L);
        metaEjemplo.setEstado(EstadoMeta.CONFIRMADA);
    }

    // ─────────────────── GET /metas ───────────────────

    @Test
    @DisplayName("GET /metas - lista todas las metas")
    void listar_retornaLista() throws Exception {
        when(metaService.listar()).thenReturn(List.of(metaEjemplo));

        mockMvc.perform(get("/metas"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].nombre").value("Aprender 50 palabras en inglés"))
                .andExpect(jsonPath("$[0].completada").value(false));

        verify(metaService).listar();
    }

    @Test
    @DisplayName("GET /metas - lista vacía retorna 200 con array vacío")
    void listar_listaVacia() throws Exception {
        when(metaService.listar()).thenReturn(List.of());

        mockMvc.perform(get("/metas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // ─────────────────── POST /metas ───────────────────

    @Test
    @DisplayName("POST /metas - crea meta válida y retorna 201")
    void crear_metaValida_retorna201() throws Exception {
        when(metaService.guardar(any(Meta.class))).thenReturn(metaEjemplo);

        mockMvc.perform(post("/metas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(metaEjemplo)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombre").value("Aprender 50 palabras en inglés"))
                .andExpect(jsonPath("$.estado").value("CONFIRMADA"));

        verify(metaService).guardar(any(Meta.class));
    }

    @Test
    @DisplayName("POST /metas - nombre vacío retorna 400")
    void crear_nombreVacio_retorna400() throws Exception {
        Meta invalida = new Meta();
        invalida.setNombre("");
        invalida.setIdUsuario(1L);

        mockMvc.perform(post("/metas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalida)))
                .andExpect(status().isBadRequest());

        verify(metaService, never()).guardar(any());
    }

    @Test
    @DisplayName("POST /metas - idUsuario null retorna 400")
    void crear_idUsuarioNull_retorna400() throws Exception {
        Meta invalida = new Meta();
        invalida.setNombre("Meta sin usuario");

        mockMvc.perform(post("/metas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalida)))
                .andExpect(status().isBadRequest());

        verify(metaService, never()).guardar(any());
    }

    @Test
    @DisplayName("POST /metas - servicio lanza RuntimeException → 400")
    void crear_servicioFalla_retorna400() throws Exception {
        when(metaService.guardar(any(Meta.class)))
                .thenThrow(new RuntimeException("Solicitud cancelada: error usuario invalido"));

        mockMvc.perform(post("/metas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(metaEjemplo)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Solicitud cancelada: error usuario invalido"));
    }

    // ─────────────────── GET /metas/{id} ───────────────────

    @Test
    @DisplayName("GET /metas/{id} - retorna meta existente")
    void obtener_existente_retorna200() throws Exception {
        when(metaService.obtener(1L)).thenReturn(metaEjemplo);

        mockMvc.perform(get("/metas/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombre").value("Aprender 50 palabras en inglés"));

        verify(metaService).obtener(1L);
    }

    @Test
    @DisplayName("GET /metas/{id} - no existe → 404")
    void obtener_noExistente_retorna404() throws Exception {
        when(metaService.obtener(99L))
                .thenThrow(new ResourceNotFoundException("Meta no encontrada con id: 99"));

        mockMvc.perform(get("/metas/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Meta no encontrada con id: 99"));
    }

    // ─────────────────── GET /metas/usuario/{id} ───────────────────

    @Test
    @DisplayName("GET /metas/usuario/{id} - retorna metas del usuario")
    void obtenerPorUsuario_retornaLista() throws Exception {
        when(metaService.obtenerPorUsuario(1L)).thenReturn(List.of(metaEjemplo));

        mockMvc.perform(get("/metas/usuario/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].idUsuario").value(1));

        verify(metaService).obtenerPorUsuario(1L);
    }

    @Test
    @DisplayName("GET /metas/usuario/{id} - sin metas → lista vacía")
    void obtenerPorUsuario_sinMetas_listaVacia() throws Exception {
        when(metaService.obtenerPorUsuario(99L)).thenReturn(List.of());

        mockMvc.perform(get("/metas/usuario/99"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // ─────────────────── DELETE /metas/{id} ───────────────────

    @Test
    @DisplayName("DELETE /metas/{id} - elimina y retorna 204")
    void eliminar_existente_retorna204() throws Exception {
        doNothing().when(metaService).eliminar(1L);

        mockMvc.perform(delete("/metas/1"))
                .andExpect(status().isNoContent());

        verify(metaService).eliminar(1L);
    }

    @Test
    @DisplayName("DELETE /metas/{id} - no existe → 404")
    void eliminar_noExistente_retorna404() throws Exception {
        doThrow(new ResourceNotFoundException("Meta no encontrada con id: 99"))
                .when(metaService).eliminar(99L);

        mockMvc.perform(delete("/metas/99"))
                .andExpect(status().isNotFound());
    }

    // ─────────────────── PUT /metas/{id} ───────────────────

    @Test
    @DisplayName("PUT /metas/{id} - actualiza y retorna meta")
    void actualizar_existente_retorna200() throws Exception {
        Meta actualizada = new Meta();
        actualizada.setNombre("Meta actualizada");
        actualizada.setDescripcion("Nueva desc");
        actualizada.setIdUsuario(1L);

        when(metaService.actualizar(eq(1L), any(Meta.class))).thenReturn(metaEjemplo);

        mockMvc.perform(put("/metas/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(actualizada)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(metaService).actualizar(eq(1L), any(Meta.class));
    }

    @Test
    @DisplayName("PUT /metas/{id} - no existe → 404")
    void actualizar_noExistente_retorna404() throws Exception {
        when(metaService.actualizar(eq(99L), any(Meta.class)))
                .thenThrow(new ResourceNotFoundException("Meta no encontrada con id: 99"));

        mockMvc.perform(put("/metas/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(metaEjemplo)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /metas/{id} - nombre vacío → 400")
    void actualizar_nombreVacio_retorna400() throws Exception {
        Meta invalida = new Meta();
        invalida.setNombre("");
        invalida.setIdUsuario(1L);

        mockMvc.perform(put("/metas/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalida)))
                .andExpect(status().isBadRequest());

        verify(metaService, never()).actualizar(any(), any());
    }
}
