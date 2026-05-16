package com.notara.usuarios.services;

import com.notara.usuarios.models.Usuario;
import com.notara.usuarios.repositories.UsuarioRepository;

import org.junit.jupiter.api.Test;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.any;

public class UsuarioServiceTest {

    private final UsuarioRepository usuarioRepository =
            mock(UsuarioRepository.class);

    private final BCryptPasswordEncoder passwordEncoder =
            mock(BCryptPasswordEncoder.class);

    private final UsuarioService usuarioService =
            new UsuarioService(usuarioRepository, passwordEncoder);

    @Test
    void guardarUsuario_ok() {

        Usuario usuario = new Usuario(
                null,
                "Juan",
                "juan@test.com",
                "1234"
        );

        when(usuarioRepository.existsByEmail(usuario.getEmail()))
                .thenReturn(false);

        when(passwordEncoder.encode("1234"))
                .thenReturn("password_encriptada");

        when(usuarioRepository.save(any(Usuario.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Usuario resultado = usuarioService.guardarUsuario(usuario);

        assertNotNull(resultado);

        assertEquals("Juan", resultado.getNombre());

        assertEquals(
                "password_encriptada",
                resultado.getPassword()
        );
    }

    @Test
    void guardarUsuario_emailDuplicado() {

        Usuario usuario = new Usuario(
                null,
                "Juan",
                "juan@test.com",
                "1234"
        );

        when(usuarioRepository.existsByEmail(usuario.getEmail()))
                .thenReturn(true);

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> usuarioService.guardarUsuario(usuario)
        );

        assertEquals(
                "El email ya está registrado",
                ex.getMessage()
        );
    }

    @Test
    void obtenerUsuarioPorId() {

        Usuario usuario = new Usuario(
                1L,
                "Juan",
                "juan@test.com",
                "1234"
        );

        when(usuarioRepository.findById(1L))
                .thenReturn(Optional.of(usuario));

        Optional<Usuario> resultado =
                usuarioService.obtenerPorId(1L);

        assertTrue(resultado.isPresent());
    }

    @Test
    void login_ok() {

        Usuario usuario = new Usuario(
                1L,
                "Juan",
                "juan@test.com",
                "hash_password"
        );

        when(usuarioRepository.findByEmail("juan@test.com"))
                .thenReturn(Optional.of(usuario));

        when(passwordEncoder.matches("1234", "hash_password"))
                .thenReturn(true);

        Usuario resultado = usuarioService.login("juan@test.com", "1234");

        assertNotNull(resultado);
        assertEquals("juan@test.com", resultado.getEmail());
    }

    @Test
    void login_usuarioNoExiste() {

        when(usuarioRepository.findByEmail("noexiste@test.com"))
                .thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> usuarioService.login("noexiste@test.com", "1234")
        );

        assertEquals("Usuario no encontrado", ex.getMessage());
    }

    @Test
    void login_passwordIncorrecta() {

        Usuario usuario = new Usuario(
                1L,
                "Juan",
                "juan@test.com",
                "hash_password"
        );

        when(usuarioRepository.findByEmail("juan@test.com"))
                .thenReturn(Optional.of(usuario));

        when(passwordEncoder.matches("wrong", "hash_password"))
                .thenReturn(false);

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> usuarioService.login("juan@test.com", "wrong")
        );

        assertEquals("Contraseña incorrecta", ex.getMessage());
    }

    @Test
    void registrarUsuario_encriptaPassword() {

        Usuario usuario = new Usuario(
                null,
                "Maria",
                "maria@test.com",
                "password123"
        );

        when(passwordEncoder.encode("password123"))
                .thenReturn("hash_encriptado");

        when(usuarioRepository.save(any(Usuario.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Usuario resultado = usuarioService.registrarUsuario(usuario);

        assertEquals("hash_encriptado", resultado.getPassword());
    }

    @Test
    void eliminarUsuario_llamaRepositorio() {

        doNothing().when(usuarioRepository).deleteById(1L);

        usuarioService.eliminarUsuario(1L);

        verify(usuarioRepository, times(1)).deleteById(1L);
    }
}
