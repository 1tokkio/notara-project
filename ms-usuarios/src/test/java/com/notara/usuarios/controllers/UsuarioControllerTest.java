package com.notara.usuarios.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.notara.usuarios.models.Usuario;
import com.notara.usuarios.services.UsuarioService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UsuarioController.class)
@AutoConfigureMockMvc(addFilters = false)
public class UsuarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UsuarioService usuarioService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void listarUsuarios() throws Exception {
        Usuario usuario = new Usuario(1L, "Juan", "Perez", "juan@test.com", "1234");

        when(usuarioService.obtenerUsuarios()).thenReturn(List.of(usuario));

        mockMvc.perform(get("/usuarios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Juan"));
    }

    @Test
    void crearUsuario() throws Exception {
        Usuario usuario = new Usuario(null, "Juan", "Perez", "juan@test.com", "1234");

        when(usuarioService.guardarUsuario(Mockito.any())).thenReturn(usuario);

        String jsonContent = objectMapper.writeValueAsString(usuario);

        mockMvc.perform(post("/usuarios")
                .contentType("application/json")
                .content(jsonContent))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Juan"));
    }
}
