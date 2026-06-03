package cl.notara.ms_notas_metas.controller;

import cl.notara.ms_notas_metas.client.UsuarioClient;
import cl.notara.ms_notas_metas.controllers.NotaController;
import cl.notara.ms_notas_metas.exceptions.GlobalExceptionHandler;
import cl.notara.ms_notas_metas.exceptions.ResourceNotFoundException;
import cl.notara.ms_notas_metas.models.EstadoNota;
import cl.notara.ms_notas_metas.models.Nota;
import cl.notara.ms_notas_metas.services.NotaService;
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

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(NotaController.class)
@Import(GlobalExceptionHandler.class)
class NotaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NotaService notaService;

    @MockBean
    private UsuarioClient usuarioCliente;

    @Autowired
    private ObjectMapper objectMapper;

    private Nota notaEjemplo;

    @BeforeEach
    void setUp() {
        notaEjemplo = new Nota();
        notaEjemplo.setId(1L);
        notaEjemplo.setTitulo("Estudiar inglés");
        notaEjemplo.setContenido("Repasar verbos");
        notaEjemplo.setIdUsuario(1L);
        notaEjemplo.setEstado(EstadoNota.CONFIRMADA);
    }

    // ─────────────────── GET /notas ───────────────────

    @Test
    @DisplayName("GET /notas - lista todas las notas")
    void listar_retornaLista() throws Exception {
        when(notaService.listar()).thenReturn(List.of(notaEjemplo));

        mockMvc.perform(get("/notas"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].titulo").value("Estudiar inglés"))
                .andExpect(jsonPath("$[0].contenido").value("Repasar verbos"));

        verify(notaService).listar();
    }

    @Test
    @DisplayName("GET /notas - lista vacía → 200 con array vacío")
    void listar_listaVacia() throws Exception {
        when(notaService.listar()).thenReturn(List.of());

        mockMvc.perform(get("/notas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // ─────────────────── POST /notas ───────────────────

    @Test
    @DisplayName("POST /notas - crea nota válida y retorna 201")
    void crear_notaValida_retorna201() throws Exception {
        when(notaService.guardar(any(Nota.class))).thenReturn(notaEjemplo);

        mockMvc.perform(post("/notas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(notaEjemplo)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.titulo").value("Estudiar inglés"))
                .andExpect(jsonPath("$.estado").value("CONFIRMADA"));

        verify(notaService).guardar(any(Nota.class));
    }

    @Test
    @DisplayName("POST /notas - título vacío → 400")
    void crear_tituloVacio_retorna400() throws Exception {
        Nota invalida = new Nota();
        invalida.setTitulo("");
        invalida.setIdUsuario(1L);

        mockMvc.perform(post("/notas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalida)))
                .andExpect(status().isBadRequest());

        verify(notaService, never()).guardar(any());
    }

    @Test
    @DisplayName("POST /notas - idUsuario null → 400")
    void crear_idUsuarioNull_retorna400() throws Exception {
        Nota invalida = new Nota();
        invalida.setTitulo("Nota sin usuario");

        mockMvc.perform(post("/notas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalida)))
                .andExpect(status().isBadRequest());

        verify(notaService, never()).guardar(any());
    }

    @Test
    @DisplayName("POST /notas - servicio lanza RuntimeException → 400")
    void crear_servicioFalla_retorna400() throws Exception {
        when(notaService.guardar(any(Nota.class)))
                .thenThrow(new RuntimeException("Solicitud cancelada: error usuario invalido"));

        mockMvc.perform(post("/notas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(notaEjemplo)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Solicitud cancelada: error usuario invalido"));
    }

    // ─────────────────── GET /notas/{id} ───────────────────

    @Test
    @DisplayName("GET /notas/{id} - retorna nota existente")
    void obtener_existente_retorna200() throws Exception {
        when(notaService.obtener(1L)).thenReturn(notaEjemplo);

        mockMvc.perform(get("/notas/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("Estudiar inglés"));

        verify(notaService).obtener(1L);
    }

    @Test
    @DisplayName("GET /notas/{id} - no existe → 404")
    void obtener_noExistente_retorna404() throws Exception {
        when(notaService.obtener(99L))
                .thenThrow(new ResourceNotFoundException("Nota no encontrada con id: 99"));

        mockMvc.perform(get("/notas/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Nota no encontrada con id: 99"));
    }

    // ─────────────────── GET /notas/usuario/{id} ───────────────────

    @Test
    @DisplayName("GET /notas/usuario/{id} - retorna notas del usuario")
    void obtenerPorUsuario_retornaLista() throws Exception {
        when(notaService.obtenerPorUsuario(1L)).thenReturn(List.of(notaEjemplo));

        mockMvc.perform(get("/notas/usuario/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].idUsuario").value(1));

        verify(notaService).obtenerPorUsuario(1L);
    }

    @Test
    @DisplayName("GET /notas/usuario/{id} - sin notas → lista vacía")
    void obtenerPorUsuario_sinNotas_listaVacia() throws Exception {
        when(notaService.obtenerPorUsuario(99L)).thenReturn(List.of());

        mockMvc.perform(get("/notas/usuario/99"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // ─────────────────── DELETE /notas/{id} ───────────────────

    @Test
    @DisplayName("DELETE /notas/{id} - elimina y retorna 204")
    void eliminar_existente_retorna204() throws Exception {
        doNothing().when(notaService).eliminar(1L);

        mockMvc.perform(delete("/notas/1"))
                .andExpect(status().isNoContent());

        verify(notaService).eliminar(1L);
    }

    @Test
    @DisplayName("DELETE /notas/{id} - no existe → 404")
    void eliminar_noExistente_retorna404() throws Exception {
        doThrow(new ResourceNotFoundException("Nota no encontrada con id: 99"))
                .when(notaService).eliminar(99L);

        mockMvc.perform(delete("/notas/99"))
                .andExpect(status().isNotFound());
    }

    // ─────────────────── PUT /notas/{id} ───────────────────

    @Test
    @DisplayName("PUT /notas/{id} - actualiza y retorna nota")
    void actualizar_existente_retorna200() throws Exception {
        Nota actualizada = new Nota();
        actualizada.setTitulo("Título actualizado");
        actualizada.setContenido("Nuevo contenido");
        actualizada.setIdUsuario(1L);

        when(notaService.actualizar(eq(1L), any(Nota.class))).thenReturn(notaEjemplo);

        mockMvc.perform(put("/notas/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(actualizada)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(notaService).actualizar(eq(1L), any(Nota.class));
    }

    @Test
    @DisplayName("PUT /notas/{id} - no existe → 404")
    void actualizar_noExistente_retorna404() throws Exception {
        when(notaService.actualizar(eq(99L), any(Nota.class)))
                .thenThrow(new ResourceNotFoundException("Nota no encontrada con id: 99"));

        mockMvc.perform(put("/notas/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(notaEjemplo)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /notas/{id} - título vacío → 400")
    void actualizar_tituloVacio_retorna400() throws Exception {
        Nota invalida = new Nota();
        invalida.setTitulo("");
        invalida.setIdUsuario(1L);

        mockMvc.perform(put("/notas/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalida)))
                .andExpect(status().isBadRequest());

        verify(notaService, never()).actualizar(any(), any());
    }
}
