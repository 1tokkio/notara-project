package cl.notara.ms_vocabulario.controllers;

import cl.notara.ms_vocabulario.exceptions.GlobalExceptionHandler;
import cl.notara.ms_vocabulario.models.Categoria;
import cl.notara.ms_vocabulario.models.Ranking;
import cl.notara.ms_vocabulario.services.RankingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class RankingControllerTest {

    @Mock
    private RankingService service;

    @InjectMocks
    private RankingController controller;

    private MockMvc mockMvc;
    private Ranking ranking;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        ranking = new Ranking();
        ranking.setId(1L);
        ranking.setIdUsuario(10L);
        ranking.setNombreUsuario("Jugador Test");
        ranking.setMejorPuntuacion(500);
        ranking.setTotalPartidas(10);
    }

    @Test
    @DisplayName("GET /vocabulario/ranking → 200 con top 10 global")
    void global_retorna200() throws Exception {
        when(service.rankingGlobal()).thenReturn(List.of(ranking));

        mockMvc.perform(get("/vocabulario/ranking"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].idUsuario").value(10))
                .andExpect(jsonPath("$[0].mejorPuntuacion").value(500));
    }

    @Test
    @DisplayName("GET /vocabulario/ranking → 200 sin registros")
    void global_sinRegistros_retorna200Vacio() throws Exception {
        when(service.rankingGlobal()).thenReturn(List.of());

        mockMvc.perform(get("/vocabulario/ranking"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("GET /vocabulario/ranking/categoria/ANIMALES → 200 con ranking de categoría")
    void porCategoria_retorna200() throws Exception {
        ranking.setCategoria(Categoria.ANIMALES);
        when(service.rankingPorCategoria(Categoria.ANIMALES)).thenReturn(List.of(ranking));

        mockMvc.perform(get("/vocabulario/ranking/categoria/ANIMALES"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].categoria").value("ANIMALES"))
                .andExpect(jsonPath("$[0].mejorPuntuacion").value(500));
    }

    @Test
    @DisplayName("GET /vocabulario/ranking/categoria/HOGAR → 200 sin registros")
    void porCategoria_sinRegistros_retorna200Vacio() throws Exception {
        when(service.rankingPorCategoria(Categoria.HOGAR)).thenReturn(List.of());

        mockMvc.perform(get("/vocabulario/ranking/categoria/HOGAR"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("GET /vocabulario/ranking/usuario/{idUsuario} → 200 con estadísticas")
    void usuario_retorna200() throws Exception {
        Ranking global = new Ranking();
        global.setIdUsuario(10L);
        global.setNombreUsuario("Jugador Test");
        global.setMejorPuntuacion(500);
        global.setTotalPartidas(10);

        Ranking porCategoria = new Ranking();
        porCategoria.setIdUsuario(10L);
        porCategoria.setCategoria(Categoria.BASICO);
        porCategoria.setMejorPuntuacion(250);

        when(service.estadisticasUsuario(10L)).thenReturn(List.of(global, porCategoria));

        mockMvc.perform(get("/vocabulario/ranking/usuario/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].idUsuario").value(10));
    }

    @Test
    @DisplayName("GET /vocabulario/ranking/usuario/{idUsuario} sin estadísticas → 200 vacío")
    void usuario_sinEstadisticas_retorna200Vacio() throws Exception {
        when(service.estadisticasUsuario(99L)).thenReturn(List.of());

        mockMvc.perform(get("/vocabulario/ranking/usuario/99"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
}
