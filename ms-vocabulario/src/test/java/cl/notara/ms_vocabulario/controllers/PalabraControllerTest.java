package cl.notara.ms_vocabulario.controllers;

import cl.notara.ms_vocabulario.exceptions.GlobalExceptionHandler;
import cl.notara.ms_vocabulario.exceptions.ResourceNotFoundException;
import cl.notara.ms_vocabulario.models.Categoria;
import cl.notara.ms_vocabulario.models.Dificultad;
import cl.notara.ms_vocabulario.models.Palabra;
import cl.notara.ms_vocabulario.repositories.PalabraRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class PalabraControllerTest {

    @Mock
    private PalabraRepository repo;

    @InjectMocks
    private PalabraController controller;

    private MockMvc mockMvc;
    private ObjectMapper mapper;
    private Palabra palabra;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        mapper = new ObjectMapper();

        palabra = new Palabra();
        palabra.setId(1L);
        palabra.setPalabra("house");
        palabra.setDefinicion("Una vivienda donde vive la gente");
        palabra.setPista("Hogar");
        palabra.setCategoria(Categoria.BASICO);
        palabra.setDificultad(Dificultad.FACIL);
        palabra.setActiva(true);
    }

    @Test
    @DisplayName("GET /vocabulario/palabras → 200 con lista")
    void listar_retorna200() throws Exception {
        when(repo.findAll()).thenReturn(List.of(palabra));

        mockMvc.perform(get("/vocabulario/palabras"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].palabra").value("house"))
                .andExpect(jsonPath("$[0].categoria").value("BASICO"));
    }

    @Test
    @DisplayName("GET /vocabulario/palabras/categorias → 200 con resumen por categoría")
    void resumenCategorias_retorna200() throws Exception {
        for (Categoria cat : Categoria.values()) {
            when(repo.countByCategoriaAndActivaTrue(cat)).thenReturn(5L);
        }

        mockMvc.perform(get("/vocabulario/palabras/categorias"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.BASICO").value(5));
    }

    @Test
    @DisplayName("GET /vocabulario/palabras/categoria/{categoria} → 200 con palabras de la categoría")
    void porCategoria_retorna200() throws Exception {
        when(repo.findByCategoriaAndActivaTrue(Categoria.BASICO)).thenReturn(List.of(palabra));

        mockMvc.perform(get("/vocabulario/palabras/categoria/BASICO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].categoria").value("BASICO"));
    }

    @Test
    @DisplayName("GET /vocabulario/palabras/{id} existente → 200")
    void obtener_existente_retorna200() throws Exception {
        when(repo.findById(1L)).thenReturn(Optional.of(palabra));

        mockMvc.perform(get("/vocabulario/palabras/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.palabra").value("house"))
                .andExpect(jsonPath("$.dificultad").value("FACIL"));
    }

    @Test
    @DisplayName("GET /vocabulario/palabras/{id} no encontrada → 404")
    void obtener_noExistente_retorna404() throws Exception {
        when(repo.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/vocabulario/palabras/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /vocabulario/palabras válido → 201")
    void crear_valido_retorna201() throws Exception {
        when(repo.save(any(Palabra.class))).thenReturn(palabra);

        mockMvc.perform(post("/vocabulario/palabras")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(palabra)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.palabra").value("house"));
    }

    @Test
    @DisplayName("PUT /vocabulario/palabras/{id} existente → 200")
    void actualizar_existente_retorna200() throws Exception {
        Palabra actualizada = new Palabra();
        actualizada.setId(1L);
        actualizada.setPalabra("home");
        actualizada.setDefinicion("Lugar donde vives");
        actualizada.setPista("Casa");
        actualizada.setCategoria(Categoria.HOGAR);
        actualizada.setDificultad(Dificultad.MEDIO);
        actualizada.setActiva(true);

        when(repo.findById(1L)).thenReturn(Optional.of(palabra));
        when(repo.save(any(Palabra.class))).thenReturn(actualizada);

        mockMvc.perform(put("/vocabulario/palabras/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(actualizada)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PUT /vocabulario/palabras/{id} no existente → 404")
    void actualizar_noExistente_retorna404() throws Exception {
        when(repo.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(put("/vocabulario/palabras/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(palabra)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /vocabulario/palabras/{id} existente → 204")
    void eliminar_existente_retorna204() throws Exception {
        when(repo.existsById(1L)).thenReturn(true);
        doNothing().when(repo).deleteById(1L);

        mockMvc.perform(delete("/vocabulario/palabras/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /vocabulario/palabras/{id} no existente → 404")
    void eliminar_noExistente_retorna404() throws Exception {
        when(repo.existsById(99L)).thenReturn(false);

        mockMvc.perform(delete("/vocabulario/palabras/99"))
                .andExpect(status().isNotFound());
    }
}
