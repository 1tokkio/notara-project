package com.notara.usuarios.services;

import com.notara.usuarios.models.Usuario;
import com.notara.usuarios.repositories.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class UsuarioServiceTest {

    private final UsuarioRepository usuarioRepository = mock(UsuarioRepository.class);
    private final UsuarioService usuarioService = new UsuarioService(usuarioRepository);

    @Test
    void guardarUsuario_ok() {
        Usuario usuario = new Usuario(null, "Juan", "Perez", "juan@test.com", "1234");

        when(usuarioRepository.existsByEmail(usuario.getEmail())).thenReturn(false);
        when(usuarioRepository.save(usuario)).thenReturn(usuario);

        Usuario resultado = usuarioService.guardarUsuario(usuario);

        assertNotNull(resultado);
        assertEquals("Juan", resultado.getNombre());
    }

    @Test
    void guardarUsuario_emailDuplicado() {
        Usuario usuario = new Usuario(null, "Juan", "Perez", "juan@test.com", "1234");

        when(usuarioRepository.existsByEmail(usuario.getEmail())).thenReturn(true);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            usuarioService.guardarUsuario(usuario);
        });

        assertEquals("El email ya está registrado", ex.getMessage());
    }

    @Test
    void obtenerUsuarioPorId() {
        Usuario usuario = new Usuario(1L, "Juan", "Perez", "juan@test.com", "1234");

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));

        Optional<Usuario> resultado = usuarioService.obtenerPorId(1L);

        assertTrue(resultado.isPresent());
    }
}
