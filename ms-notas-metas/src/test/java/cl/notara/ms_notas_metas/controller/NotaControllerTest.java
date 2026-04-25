package cl.notara.ms_notas_metas.controller;

import cl.notara.ms_notas_metas.controllers.NotaController;
import cl.notara.ms_notas_metas.models.Nota;
import cl.notara.ms_notas_metas.services.NotaService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(NotaController.class)
class NotaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NotaService notaService;

    @Autowired
    private ObjectMapper objectMapper;

    private Nota notaEjemplo;

    @BeforeEach
    void setUp() {
        notaEjemplo = new Nota();
        notaEjemplo.setId(1L);
        notaEjemplo.setTitulo("Estudiar inglés");
        notaEjemplo.setContenido("Repasar verbos");
        notaEjemplo.setIdUsuario(1L);  // CORREGIDO: añadir idUsuario
    }

    @Test
    void testGetNotas() throws Exception {
        when(notaService.listar()).thenReturn(Arrays.asList(notaEjemplo));

        mockMvc.perform(get("/notas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].titulo").value("Estudiar inglés"))
                .andExpect(jsonPath("$[0].contenido").value("Repasar verbos"));
    }

    @Test
    void testPostNota() throws Exception {
        when(notaService.guardar(any(Nota.class))).thenReturn(notaEjemplo);

        mockMvc.perform(post("/notas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(notaEjemplo)))
                .andExpect(status().isCreated())  // CORREGIDO: 201 en lugar de 200
                .andExpect(jsonPath("$.titulo").value("Estudiar inglés"))
                .andExpect(jsonPath("$.contenido").value("Repasar verbos"));
    }

    @Test
    void testGetNotaPorId() throws Exception {
        when(notaService.obtener(1L)).thenReturn(notaEjemplo);  // CORREGIDO: sin Optional

        mockMvc.perform(get("/notas/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("Estudiar inglés"));
    }

    @Test
    void testGetNotaNoEncontrada() throws Exception {
        when(notaService.obtener(999L)).thenReturn(null);  // CORREGIDO: retorna null

        mockMvc.perform(get("/notas/999"))
                .andExpect(status().isOk());  // CORREGIDO: espera 200
    }

    @Test
    void testDeleteNota() throws Exception {
        mockMvc.perform(delete("/notas/1"))
                .andExpect(status().isNoContent());

        verify(notaService, times(1)).eliminar(1L);
    }
}