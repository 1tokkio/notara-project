package cl.notara.ms_notas_metas.service;

import cl.notara.ms_notas_metas.exceptions.ResourceNotFoundException;
import cl.notara.ms_notas_metas.models.Nota;
import cl.notara.ms_notas_metas.repositories.NotaRepository;
import cl.notara.ms_notas_metas.services.NotaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotaServiceTest {

    @Mock
    private NotaRepository notaRepository;

    @InjectMocks
    private NotaService notaService;

    private Nota notaEjemplo;

    @BeforeEach
    void setUp() {
        notaEjemplo = new Nota();
        notaEjemplo.setId(1L);
        notaEjemplo.setTitulo("Nota 1");
        notaEjemplo.setContenido("Estudiar inglés");
        notaEjemplo.setIdUsuario(1L);  // CORREGIDO: añadir idUsuario
    }

    @Test
    void testListar() {
        // Preparar
        when(notaRepository.findAll()).thenReturn(Arrays.asList(notaEjemplo));

        // Ejecutar
        var resultado = notaService.listar();

        // Verificar
        assertEquals(1, resultado.size());
        assertEquals("Nota 1", resultado.get(0).getTitulo());
        verify(notaRepository, times(1)).findAll();
    }

    @Test
    void testGuardar() {
        // Preparar
        when(notaRepository.save(any(Nota.class))).thenReturn(notaEjemplo);

        // Ejecutar
        Nota resultado = notaService.guardar(notaEjemplo);

        // Verificar
        assertNotNull(resultado);
        assertEquals("Nota 1", resultado.getTitulo());
        verify(notaRepository, times(1)).save(notaEjemplo);
    }

    @Test
    void testObtenerCuandoExiste() {
        // Preparar
        when(notaRepository.findById(1L)).thenReturn(Optional.of(notaEjemplo));

        // Ejecutar - CORREGIDO: retorna Nota, no Optional
        Nota resultado = notaService.obtener(1L);

        // Verificar
        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals("Nota 1", resultado.getTitulo());
        verify(notaRepository, times(1)).findById(1L);
    }

    @Test
    void testObtenerCuandoNoExiste() {
        // Preparar
        when(notaRepository.findById(99L)).thenReturn(Optional.empty());

        // Ejecutar & Verificar - CORREGIDO: espera excepción
        assertThrows(ResourceNotFoundException.class, () -> {
            notaService.obtener(99L);
        });

        verify(notaRepository, times(1)).findById(99L);
    }

    @Test
    void testEliminar() {
        // Preparar - CORREGIDO: añadir mock para existsById
        when(notaRepository.existsById(1L)).thenReturn(true);
        doNothing().when(notaRepository).deleteById(1L);

        // Ejecutar
        notaService.eliminar(1L);

        // Verificar
        verify(notaRepository, times(1)).existsById(1L);
        verify(notaRepository, times(1)).deleteById(1L);
    }
}