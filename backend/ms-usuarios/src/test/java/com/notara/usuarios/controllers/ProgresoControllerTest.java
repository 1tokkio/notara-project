package com.notara.usuarios.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.notara.usuarios.dto.ProgresoDto;
import com.notara.usuarios.models.Progreso;
import com.notara.usuarios.services.ProgresoService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProgresoController.class)
@AutoConfigureMockMvc
class ProgresoControllerTest {

    @Autowired private MockMvc      mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean private ProgresoService progresoService;
    @MockBean private com.notara.usuarios.security.JwtService jwtService;

    @Test
    @DisplayName("GET /progress/stats - retorna progreso del usuario autenticado")
    @WithMockUser(username = "user@test.com", roles = "USER")
    void getStats_ok() throws Exception {
        Progreso p = new Progreso();
        p.setUsuarioEmail("user@test.com");
        p.setXp(300);
        p.setStreak(5);

        when(progresoService.getOrCreate("user@test.com")).thenReturn(p);

        mockMvc.perform(get("/progress/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.usuarioEmail").value("user@test.com"))
                .andExpect(jsonPath("$.xp").value(300))
                .andExpect(jsonPath("$.streak").value(5));
    }

    @Test
    @DisplayName("POST /progress/sync - sincroniza progreso y retorna 200")
    @WithMockUser(username = "user@test.com", roles = "USER")
    void sync_ok() throws Exception {
        ProgresoDto dto = new ProgresoDto();
        dto.setXp(500);
        dto.setStreak(10);

        Progreso synced = new Progreso();
        synced.setUsuarioEmail("user@test.com");
        synced.setXp(500);
        synced.setStreak(10);

        when(progresoService.sync(eq("user@test.com"), any(ProgresoDto.class)))
                .thenReturn(synced);

        mockMvc.perform(post("/progress/sync").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.xp").value(500))
                .andExpect(jsonPath("$.streak").value(10));
    }
}
