package cl.notara.ms_notas_metas.controller;

import cl.notara.ms_notas_metas.controllers.MetaController;
import cl.notara.ms_notas_metas.models.Meta;
import cl.notara.ms_notas_metas.services.MetaService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MetaController.class)
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
    }

    @Test
    @DisplayName("GET /metas - Debería listar todas las metas")
    void testListarMetas() throws Exception {
        when(metaService.listar()).thenReturn(Arrays.asList(metaEjemplo));

        mockMvc.perform(get("/metas"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].nombre").value("Aprender 50 palabras en inglés"))
                .andExpect(jsonPath("$[0].completada").value(false));

        verify(metaService, times(1)).listar();
    }

    @Test
    @DisplayName("POST /metas - Debería crear una nueva meta")
    void testCrearMeta() throws Exception {
        metaEjemplo.setIdUsuario(1L);

        when(metaService.guardar(any(Meta.class))).thenReturn(metaEjemplo);

        mockMvc.perform(post("/metas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(metaEjemplo)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombre").value("Aprender 50 palabras en inglés"));

        verify(metaService, times(1)).guardar(any(Meta.class));
    }

    @Test
    @DisplayName("GET /metas/{id} - Debería obtener una meta existente")
    void testObtenerMetaExistente() throws Exception {
        // Given
        when(metaService.obtener(1L)).thenReturn(metaEjemplo);

        // When & Then
        mockMvc.perform(get("/metas/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombre").value("Aprender 50 palabras en inglés"));

        verify(metaService, times(1)).obtener(1L);
    }

    @Test
    @DisplayName("GET /metas/{id} - Debería retornar 200 incluso cuando la meta no existe")
    void testObtenerMetaNoExistente() throws Exception {
        // Given
        when(metaService.obtener(99L)).thenReturn(null);

        // When & Then - El controller retorna 200 con body vacío
        mockMvc.perform(get("/metas/99"))
                .andExpect(status().isOk());

        verify(metaService, times(1)).obtener(99L);
    }

    @Test
    @DisplayName("DELETE /metas/{id} - Debería eliminar una meta")
    void testEliminarMeta() throws Exception {
        mockMvc.perform(delete("/metas/1"))
                .andExpect(status().isNoContent());

        verify(metaService, times(1)).eliminar(1L);
    }

    @Test
    @DisplayName("POST /metas - Debería validar que el nombre no esté vacío")
    void testCrearMetaConNombreInvalido() throws Exception {
        Meta metaInvalida = new Meta();
        metaInvalida.setNombre("");
        metaInvalida.setDescripcion("Descripción");
        metaInvalida.setIdUsuario(1L);

        mockMvc.perform(post("/metas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(metaInvalida)))
                .andExpect(status().isBadRequest());

        verify(metaService, never()).guardar(any());
    }
}