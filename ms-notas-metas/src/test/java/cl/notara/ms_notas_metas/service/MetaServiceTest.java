package cl.notara.ms_notas_metas.service;

import cl.notara.ms_notas_metas.client.UsuarioClient;
import cl.notara.ms_notas_metas.dto.UsuarioDTO;
import cl.notara.ms_notas_metas.exceptions.ResourceNotFoundException;
import cl.notara.ms_notas_metas.models.EstadoMeta;
import cl.notara.ms_notas_metas.models.Meta;
import cl.notara.ms_notas_metas.repositories.MetaRepository;
import cl.notara.ms_notas_metas.services.MetaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MetaServiceTest {

    @Mock
    private MetaRepository metaRepository;

    @Mock
    private UsuarioClient usuarioCliente;

    @InjectMocks
    private MetaService metaService;

    private Meta metaEjemplo;

    @BeforeEach
    void setUp() {
        metaEjemplo = new Meta();
        metaEjemplo.setId(1L);
        metaEjemplo.setNombre("Aprender 50 palabras en inglés");
        metaEjemplo.setDescripcion("Estudiar vocabulario básico");
        metaEjemplo.setFechaLimite(LocalDate.of(2025, 12, 31));
        metaEjemplo.setCompletada(false);
        metaEjemplo.setIdUsuario(1L);
        metaEjemplo.setEstado(EstadoMeta.PENDIENTE);
    }

    // ─────────────────── listar() ───────────────────

    @Test
    @DisplayName("listar() - retorna todas las metas")
    void listar_retornaLista() {
        List<Meta> esperadas = Arrays.asList(metaEjemplo, new Meta());
        when(metaRepository.findAll()).thenReturn(esperadas);

        List<Meta> resultado = metaService.listar();

        assertThat(resultado).hasSize(2).contains(metaEjemplo);
        verify(metaRepository).findAll();
    }

    @Test
    @DisplayName("listar() - retorna lista vacía si no hay metas")
    void listar_listaVacia() {
        when(metaRepository.findAll()).thenReturn(List.of());

        List<Meta> resultado = metaService.listar();

        assertThat(resultado).isEmpty();
    }

    // ─────────────────── guardar() ───────────────────

    @Test
    @DisplayName("guardar() - usuario válido → estado CONFIRMADA")
    void guardar_usuarioValido_retornaMetaConfirmada() {
        UsuarioDTO usuario = new UsuarioDTO("Juan", 1L);
        // Primera llamada: save en PENDIENTE; segunda: save en CONFIRMADA
        when(metaRepository.save(any(Meta.class))).thenReturn(metaEjemplo);
        when(usuarioCliente.getUsuario(1L)).thenReturn(usuario);

        Meta resultado = metaService.guardar(metaEjemplo);

        assertThat(resultado).isNotNull();
        verify(metaRepository, times(2)).save(any(Meta.class));
        verify(usuarioCliente).getUsuario(1L);
    }

    @Test
    @DisplayName("guardar() - usuario null → lanza RuntimeException y borra meta")
    void guardar_usuarioNull_lanzaExcepcion() {
        when(metaRepository.save(any(Meta.class))).thenReturn(metaEjemplo);
        when(usuarioCliente.getUsuario(1L)).thenReturn(null);
        doNothing().when(metaRepository).deleteById(1L);

        assertThrows(RuntimeException.class, () -> metaService.guardar(metaEjemplo));

        verify(metaRepository).deleteById(1L);
    }

    @Test
    @DisplayName("guardar() - Feign lanza excepción → lanza RuntimeException y borra meta")
    void guardar_clienteLanzaExcepcion_lanzaRuntimeException() {
        when(metaRepository.save(any(Meta.class))).thenReturn(metaEjemplo);
        when(usuarioCliente.getUsuario(1L)).thenThrow(new RuntimeException("Feign error"));
        doNothing().when(metaRepository).deleteById(1L);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> metaService.guardar(metaEjemplo));

        assertThat(ex.getMessage()).contains("Solicitud cancelada");
        verify(metaRepository).deleteById(1L);
    }

    // ─────────────────── obtener() ───────────────────

    @Test
    @DisplayName("obtener() - existe → retorna la meta")
    void obtener_existente_retornaMeta() {
        when(metaRepository.findById(1L)).thenReturn(Optional.of(metaEjemplo));

        Meta resultado = metaService.obtener(1L);

        assertThat(resultado.getNombre()).isEqualTo("Aprender 50 palabras en inglés");
        verify(metaRepository).findById(1L);
    }

    @Test
    @DisplayName("obtener() - no existe → lanza ResourceNotFoundException")
    void obtener_noExistente_lanzaExcepcion() {
        when(metaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> metaService.obtener(99L));
        verify(metaRepository).findById(99L);
    }

    // ─────────────────── obtenerPorUsuario() ───────────────────

    @Test
    @DisplayName("obtenerPorUsuario() - retorna metas del usuario")
    void obtenerPorUsuario_retornaLista() {
        when(metaRepository.findByIdUsuario(1L)).thenReturn(List.of(metaEjemplo));

        List<Meta> resultado = metaService.obtenerPorUsuario(1L);

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getIdUsuario()).isEqualTo(1L);
        verify(metaRepository).findByIdUsuario(1L);
    }

    @Test
    @DisplayName("obtenerPorUsuario() - sin metas → lista vacía")
    void obtenerPorUsuario_sinMetas_listaVacia() {
        when(metaRepository.findByIdUsuario(99L)).thenReturn(List.of());

        List<Meta> resultado = metaService.obtenerPorUsuario(99L);

        assertThat(resultado).isEmpty();
    }

    // ─────────────────── eliminar() ───────────────────

    @Test
    @DisplayName("eliminar() - existe → elimina correctamente")
    void eliminar_existente_eliminaOk() {
        when(metaRepository.existsById(1L)).thenReturn(true);
        doNothing().when(metaRepository).deleteById(1L);

        metaService.eliminar(1L);

        verify(metaRepository).existsById(1L);
        verify(metaRepository).deleteById(1L);
    }

    @Test
    @DisplayName("eliminar() - no existe → lanza ResourceNotFoundException")
    void eliminar_noExistente_lanzaExcepcion() {
        when(metaRepository.existsById(99L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> metaService.eliminar(99L));
        verify(metaRepository, never()).deleteById(any());
    }

    // ─────────────────── actualizar() ───────────────────

    @Test
    @DisplayName("actualizar() - existe → actualiza campos y retorna meta")
    void actualizar_existente_actualizaYRetorna() {
        Meta nuevaData = new Meta();
        nuevaData.setNombre("Meta actualizada");
        nuevaData.setDescripcion("Nueva desc");
        nuevaData.setFechaLimite(LocalDate.of(2026, 6, 1));
        nuevaData.setCompletada(true);
        nuevaData.setIdUsuario(2L);

        when(metaRepository.findById(1L)).thenReturn(Optional.of(metaEjemplo));
        when(metaRepository.save(any(Meta.class))).thenReturn(metaEjemplo);

        Meta resultado = metaService.actualizar(1L, nuevaData);

        assertThat(resultado).isNotNull();
        verify(metaRepository).findById(1L);
        verify(metaRepository).save(metaEjemplo);
    }

    @Test
    @DisplayName("actualizar() - no existe → lanza ResourceNotFoundException")
    void actualizar_noExistente_lanzaExcepcion() {
        when(metaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> metaService.actualizar(99L, metaEjemplo));
        verify(metaRepository, never()).save(any());
    }
}
