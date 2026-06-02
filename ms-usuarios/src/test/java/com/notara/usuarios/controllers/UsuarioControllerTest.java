package com.notara.usuarios.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.notara.usuarios.models.Usuario;
import com.notara.usuarios.security.JwtFilter;
import com.notara.usuarios.security.JwtService;
import com.notara.usuarios.services.UsuarioService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = UsuarioController.class)
@AutoConfigureMockMvc(addFilters = false)
class UsuarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UsuarioService usuarioService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private JwtFilter jwtFilter;

    @Test
    @DisplayName("GET /usuarios - retorna lista de usuarios")
    @WithMockUser
    void listarUsuarios_ok() throws Exception {

        List<Usuario> lista = List.of(
                new Usuario(1L, "Juan", "juan@test.com", "hash"),
                new Usuario(2L, "Ana", "ana@test.com", "hash")
        );

        when(usuarioService.obtenerUsuarios()).thenReturn(lista);

        mockMvc.perform(get("/usuarios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Juan"))
                .andExpect(jsonPath("$[1].nombre").value("Ana"));
    }

    @Test
    @DisplayName("POST /usuarios - crea usuario válido")
    @WithMockUser
    void crearUsuario_ok() throws Exception {

        Usuario usuario =
                new Usuario(null, "Juan", "juan@test.com", "1234");

        when(usuarioService.guardarUsuario(any()))
                .thenReturn(usuario);

        mockMvc.perform(post("/usuarios")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(usuario)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Juan"))
                .andExpect(jsonPath("$.email").value("juan@test.com"));
    }

    @Test
    @DisplayName("POST /usuarios - retorna 400 si hay errores de validación")
    @WithMockUser
    void crearUsuario_validacionFalla() throws Exception {

        String invalidJson =
                "{\"nombre\":\"\",\"email\":\"no-es-email\",\"password\":\"\"}";

        mockMvc.perform(post("/usuarios")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /usuarios/{id} - retorna usuario cuando existe")
    @WithMockUser
    void obtenerUsuario_encontrado() throws Exception {

        Usuario usuario =
                new Usuario(1L, "Juan", "juan@test.com", "hash");

        when(usuarioService.obtenerPorId(1L))
                .thenReturn(Optional.of(usuario));

        mockMvc.perform(get("/usuarios/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Juan"));
    }

    @Test
    @DisplayName("GET /usuarios/{id} - retorna 404 cuando no existe")
    @WithMockUser
    void obtenerUsuario_noEncontrado() throws Exception {

        when(usuarioService.obtenerPorId(99L))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/usuarios/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /usuarios/{id} - elimina y retorna 204")
    @WithMockUser
    void eliminarUsuario_ok() throws Exception {

        doNothing().when(usuarioService)
                .eliminarUsuario(1L);

        mockMvc.perform(delete("/usuarios/1")
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(usuarioService, times(1))
                .eliminarUsuario(1L);
    }

    @Test
    @DisplayName("GET /usuarios/me - retorna el email autenticado")
    void me_retornaEmailAutenticado() throws Exception {

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        "juan@test.com",
                        null,
                        List.of()
                );

        mockMvc.perform(
                        get("/usuarios/me")
                                .with(request -> {
                                    request.setUserPrincipal(authentication);
                                    return request;
                                })
                )
                .andExpect(status().isOk())
                .andExpect(content().string("juan@test.com"));
    }
}