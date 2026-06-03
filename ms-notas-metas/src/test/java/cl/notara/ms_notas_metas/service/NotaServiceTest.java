package cl.notara.ms_notas_metas.service;

import cl.notara.ms_notas_metas.client.UsuarioClient;
import cl.notara.ms_notas_metas.dto.UsuarioDTO;
import cl.notara.ms_notas_metas.exceptions.ResourceNotFoundException;
import cl.notara.ms_notas_metas.models.EstadoNota;
import cl.notara.ms_notas_metas.models.Nota;
import cl.notara.ms_notas_metas.repositories.NotaRepository;
import cl.notara.ms_notas_metas.services.NotaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotaServiceTest {

    @Mock
    private NotaRepository notaRepository;

    @Mock
    private UsuarioClient usuarioCliente;

    @InjectMocks
    private NotaService notaService;

    private Nota notaEjemplo;

    @BeforeEach
    void setUp() {
        notaEjemplo = new Nota();
        notaEjemplo.setId(1L);
        notaEjemplo.setTitulo("Nota 1");
        notaEjemplo.setContenido("Estudiar inglés");
        notaEjemplo.setIdUsuario(1L);
        notaEjemplo.setEstado(EstadoNota.PENDIENTE);
    }

    // ─────────────────── listar() ───────────────────

    @Test
    @DisplayName("listar() - retorna todas las notas")
    void listar_retornaLista() {
        when(notaRepository.findAll()).thenReturn(Arrays.asList(notaEjemplo));

        List<Nota> resultado = notaService.listar();

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getTitulo()).isEqualTo("Nota 1");
        verify(notaRepository).findAll();
    }

    @Test
    @DisplayName("listar() - retorna lista vacía si no hay notas")
    void listar_listaVacia() {
        when(notaRepository.findAll()).thenReturn(List.of());

        assertThat(notaService.listar()).isEmpty();
    }

    // ─────────────────── guardar() ───────────────────

    @Test
    @DisplayName("guardar() - usuario válido → estado CONFIRMADA")
    void guardar_usuarioValido_retornaNotaConfirmada() {
        UsuarioDTO usuario = new UsuarioDTO("Juan", 1L);
        when(notaRepository.save(any(Nota.class))).thenReturn(notaEjemplo);
        when(usuarioCliente.getUsuario(1L)).thenReturn(usuario);

        Nota resultado = notaService.guardar(notaEjemplo);

        assertThat(resultado).isNotNull();
        verify(notaRepository, times(2)).save(any(Nota.class));
        verify(usuarioCliente).getUsuario(1L);
    }

    @Test
    @DisplayName("guardar() - usuario null → lanza RuntimeException y borra nota")
    void guardar_usuarioNull_lanzaExcepcion() {
        when(notaRepository.save(any(Nota.class))).thenReturn(notaEjemplo);
        when(usuarioCliente.getUsuario(1L)).thenReturn(null);
        doNothing().when(notaRepository).deleteById(1L);

        assertThrows(RuntimeException.class, () -> notaService.guardar(notaEjemplo));

        verify(notaRepository).deleteById(1L);
    }

    @Test
    @DisplayName("guardar() - Feign lanza excepción → lanza RuntimeException y borra nota")
    void guardar_clienteLanzaExcepcion_lanzaRuntimeException() {
        when(notaRepository.save(any(Nota.class))).thenReturn(notaEjemplo);
        when(usuarioCliente.getUsuario(1L)).thenThrow(new RuntimeException("Feign error"));
        doNothing().when(notaRepository).deleteById(1L);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> notaService.guardar(notaEjemplo));

        assertThat(ex.getMessage()).contains("Solicitud cancelada");
        verify(notaRepository).deleteById(1L);
    }

    // ─────────────────── obtener() ───────────────────

    @Test
    @DisplayName("obtener() - existe → retorna la nota")
    void obtener_existente_retornaNota() {
        when(notaRepository.findById(1L)).thenReturn(Optional.of(notaEjemplo));

        Nota resultado = notaService.obtener(1L);

        assertThat(resultado.getTitulo()).isEqualTo("Nota 1");
        verify(notaRepository).findById(1L);
    }

    @Test
    @DisplayName("obtener() - no existe → lanza ResourceNotFoundException")
    void obtener_noExistente_lanzaExcepcion() {
        when(notaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> notaService.obtener(99L));
        verify(notaRepository).findById(99L);
    }

    // ─────────────────── obtenerPorUsuario() ───────────────────

    @Test
    @DisplayName("obtenerPorUsuario() - retorna notas del usuario")
    void obtenerPorUsuario_retornaLista() {
        when(notaRepository.findByIdUsuario(1L)).thenReturn(List.of(notaEjemplo));

        List<Nota> resultado = notaService.obtenerPorUsuario(1L);

        assertThat(resultado).hasSize(1);
        verify(notaRepository).findByIdUsuario(1L);
    }

    @Test
    @DisplayName("obtenerPorUsuario() - sin notas → lista vacía")
    void obtenerPorUsuario_sinNotas_listaVacia() {
        when(notaRepository.findByIdUsuario(99L)).thenReturn(List.of());

        assertThat(notaService.obtenerPorUsuario(99L)).isEmpty();
    }

    // ─────────────────── eliminar() ───────────────────

    @Test
    @DisplayName("eliminar() - existe → elimina correctamente")
    void eliminar_existente_eliminaOk() {
        when(notaRepository.existsById(1L)).thenReturn(true);
        doNothing().when(notaRepository).deleteById(1L);

        notaService.eliminar(1L);

        verify(notaRepository).existsById(1L);
        verify(notaRepository).deleteById(1L);
    }

    @Test
    @DisplayName("eliminar() - no existe → lanza ResourceNotFoundException")
    void eliminar_noExistente_lanzaExcepcion() {
        when(notaRepository.existsById(99L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> notaService.eliminar(99L));
        verify(notaRepository, never()).deleteById(any());
    }

    // ─────────────────── actualizar() ───────────────────

    @Test
    @DisplayName("actualizar() - existe → actualiza campos y retorna nota")
    void actualizar_existente_actualizaYRetorna() {
        Nota nuevaData = new Nota();
        nuevaData.setTitulo("Título actualizado");
        nuevaData.setContenido("Nuevo contenido");
        nuevaData.setIdUsuario(2L);

        when(notaRepository.findById(1L)).thenReturn(Optional.of(notaEjemplo));
        when(notaRepository.save(any(Nota.class))).thenReturn(notaEjemplo);

        Nota resultado = notaService.actualizar(1L, nuevaData);

        assertThat(resultado).isNotNull();
        verify(notaRepository).findById(1L);
        verify(notaRepository).save(notaEjemplo);
    }

    @Test
    @DisplayName("actualizar() - no existe → lanza ResourceNotFoundException")
    void actualizar_noExistente_lanzaExcepcion() {
        when(notaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> notaService.actualizar(99L, notaEjemplo));
        verify(notaRepository, never()).save(any());
    }
}
