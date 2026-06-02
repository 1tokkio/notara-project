package com.notara.usuarios.services;

import com.notara.usuarios.models.Usuario;
import com.notara.usuarios.repositories.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.any;

class UsuarioServiceTest {

    private UsuarioRepository usuarioRepository;
    private BCryptPasswordEncoder passwordEncoder;
    private UsuarioService usuarioService;

    @BeforeEach
    void setUp() {
        usuarioRepository = mock(UsuarioRepository.class);
        passwordEncoder   = mock(BCryptPasswordEncoder.class);
        usuarioService    = new UsuarioService(usuarioRepository, passwordEncoder);
    }

    // ──────────────────────── obtenerUsuarios ────────────────────────

    @Test
    @DisplayName("obtenerUsuarios - retorna lista de usuarios")
    void obtenerUsuarios_retornaLista() {
        List<Usuario> lista = List.of(
                new Usuario(1L, "Juan", "juan@test.com", "hash"),
                new Usuario(2L, "Ana",  "ana@test.com",  "hash")
        );
        when(usuarioRepository.findAll()).thenReturn(lista);

        List<Usuario> resultado = usuarioService.obtenerUsuarios();

        assertEquals(2, resultado.size());
        verify(usuarioRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("obtenerUsuarios - retorna lista vacía cuando no hay usuarios")
    void obtenerUsuarios_listaVacia() {
        when(usuarioRepository.findAll()).thenReturn(List.of());

        List<Usuario> resultado = usuarioService.obtenerUsuarios();

        assertTrue(resultado.isEmpty());
    }

    // ──────────────────────── guardarUsuario ─────────────────────────

    @Test
    @DisplayName("guardarUsuario - guarda correctamente cuando email no existe")
    void guardarUsuario_ok() {
        Usuario usuario = new Usuario(null, "Juan", "juan@test.com", "1234");

        when(usuarioRepository.existsByEmail(usuario.getEmail())).thenReturn(false);
        when(usuarioRepository.save(any(Usuario.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        Usuario resultado = usuarioService.guardarUsuario(usuario);

        assertNotNull(resultado);
        assertEquals("Juan", resultado.getNombre());
        verify(usuarioRepository).save(usuario);
    }

    @Test
    @DisplayName("guardarUsuario - lanza excepción cuando email ya está registrado")
    void guardarUsuario_emailDuplicado() {
        Usuario usuario = new Usuario(null, "Juan", "juan@test.com", "1234");

        when(usuarioRepository.existsByEmail(usuario.getEmail())).thenReturn(true);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> usuarioService.guardarUsuario(usuario));

        assertEquals("El email ya está registrado", ex.getMessage());
        verify(usuarioRepository, never()).save(any());
    }

    // ──────────────────────── obtenerPorId ───────────────────────────

    @Test
    @DisplayName("obtenerPorId - retorna usuario cuando existe")
    void obtenerPorId_encontrado() {
        Usuario usuario = new Usuario(1L, "Juan", "juan@test.com", "hash");
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));

        Optional<Usuario> resultado = usuarioService.obtenerPorId(1L);

        assertTrue(resultado.isPresent());
        assertEquals("Juan", resultado.get().getNombre());
    }

    @Test
    @DisplayName("obtenerPorId - retorna Optional vacío cuando no existe")
    void obtenerPorId_noEncontrado() {
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<Usuario> resultado = usuarioService.obtenerPorId(99L);

        assertFalse(resultado.isPresent());
    }

    // ──────────────────────── eliminarUsuario ────────────────────────

    @Test
    @DisplayName("eliminarUsuario - llama al repositorio con el id correcto")
    void eliminarUsuario_llamaRepositorio() {
        doNothing().when(usuarioRepository).deleteById(1L);

        usuarioService.eliminarUsuario(1L);

        verify(usuarioRepository, times(1)).deleteById(1L);
    }

    // ──────────────────────── registrarUsuario ───────────────────────

    @Test
    @DisplayName("registrarUsuario - encripta la contraseña antes de guardar")
    void registrarUsuario_encriptaPassword() {
        Usuario usuario = new Usuario(null, "Maria", "maria@test.com", "password123");

        when(passwordEncoder.encode("password123")).thenReturn("hash_encriptado");
        when(usuarioRepository.save(any(Usuario.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        Usuario resultado = usuarioService.registrarUsuario(usuario);

        assertEquals("hash_encriptado", resultado.getPassword());
        verify(passwordEncoder).encode("password123");
        verify(usuarioRepository).save(usuario);
    }

    // ──────────────────────── login ──────────────────────────────────

    @Test
    @DisplayName("login - retorna usuario con credenciales correctas")
    void login_ok() {
        Usuario usuario = new Usuario(1L, "Juan", "juan@test.com", "hash_password");

        when(usuarioRepository.findByEmail("juan@test.com"))
                .thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("1234", "hash_password")).thenReturn(true);

        Usuario resultado = usuarioService.login("juan@test.com", "1234");

        assertNotNull(resultado);
        assertEquals("juan@test.com", resultado.getEmail());
    }

    @Test
    @DisplayName("login - lanza excepción si el usuario no existe")
    void login_usuarioNoExiste() {
        when(usuarioRepository.findByEmail("noexiste@test.com"))
                .thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> usuarioService.login("noexiste@test.com", "1234"));

        assertEquals("Usuario no encontrado", ex.getMessage());
    }

    @Test
    @DisplayName("login - lanza excepción si la contraseña es incorrecta")
    void login_passwordIncorrecta() {
        Usuario usuario = new Usuario(1L, "Juan", "juan@test.com", "hash_password");

        when(usuarioRepository.findByEmail("juan@test.com"))
                .thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("wrong", "hash_password")).thenReturn(false);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> usuarioService.login("juan@test.com", "wrong"));

        assertEquals("Contraseña incorrecta", ex.getMessage());
    }
}
