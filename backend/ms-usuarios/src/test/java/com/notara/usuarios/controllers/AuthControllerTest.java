package com.notara.usuarios.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.notara.usuarios.dto.LoginRequest;
import com.notara.usuarios.dto.RegisterRequest;
import com.notara.usuarios.models.Usuario;
import com.notara.usuarios.security.JwtService;
import com.notara.usuarios.services.UsuarioService;
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
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired private MockMvc      mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private UsuarioService usuarioService;
    @MockBean private JwtService     jwtService;

    // ──────────────────── POST /auth/register ────────────────────

    @Test
    @DisplayName("POST /auth/register - registra usuario y retorna 200")
    @WithMockUser
    void register_ok() throws Exception {
        Usuario saved = new Usuario(1L, "Ana", "ana@test.com", "hash");
        when(usuarioService.registrarUsuario(any())).thenReturn(saved);

        RegisterRequest req = new RegisterRequest();
        req.setNombre("Ana");
        req.setEmail("ana@test.com");
        req.setPassword("secret123");

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Ana"))
                .andExpect(jsonPath("$.email").value("ana@test.com"));
    }

    @Test
    @DisplayName("POST /auth/register - retorna 400 con datos inválidos")
    @WithMockUser
    void register_datosInvalidos() throws Exception {
        String badJson = "{\"nombre\":\"\",\"email\":\"no-email\",\"password\":\"\"}";

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(badJson))
                .andExpect(status().isBadRequest());
    }

    // ──────────────────── POST /auth/login ────────────────────

    @Test
    @DisplayName("POST /auth/login - retorna tokens y datos de usuario")
    @WithMockUser
    void login_ok() throws Exception {
        Usuario usuario = new Usuario(1L, "Ana", "ana@test.com", "hash");

        when(usuarioService.login("ana@test.com", "secret123")).thenReturn(usuario);
        when(jwtService.generateToken("ana@test.com")).thenReturn("access-token");
        when(jwtService.generateRefreshToken("ana@test.com")).thenReturn("refresh-token");

        LoginRequest req = new LoginRequest();
        req.setEmail("ana@test.com");
        req.setPassword("secret123");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-token"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token"))
                .andExpect(jsonPath("$.user.email").value("ana@test.com"))
                .andExpect(jsonPath("$.user.name").value("Ana"));
    }

    @Test
    @DisplayName("POST /auth/login - retorna 400 cuando el servicio lanza excepción")
    @WithMockUser
    void login_credencialesInvalidas() throws Exception {
        when(usuarioService.login("bad@test.com", "wrong"))
                .thenThrow(new RuntimeException("Contraseña incorrecta"));

        LoginRequest req = new LoginRequest();
        req.setEmail("bad@test.com");
        req.setPassword("wrong");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Contraseña incorrecta"));
    }
}
