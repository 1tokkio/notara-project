package cl.notara.ms_notas_metas.service;

import cl.notara.ms_notas_metas.exceptions.ResourceNotFoundException;
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
        metaEjemplo.setIdUsuario(1L); // Añadido
    }

    @Test
    @DisplayName("Debería listar todas las metas")
    void testListar() {
        // Given
        List<Meta> metasEsperadas = Arrays.asList(metaEjemplo, new Meta());
        when(metaRepository.findAll()).thenReturn(metasEsperadas);

        // When
        List<Meta> resultado = metaService.listar();

        // Then
        assertThat(resultado).hasSize(2);
        assertThat(resultado).contains(metaEjemplo);
        verify(metaRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Debería guardar una meta correctamente")
    void testGuardar() {
        // Given
        when(metaRepository.save(any(Meta.class))).thenReturn(metaEjemplo);

        // When
        Meta resultado = metaService.guardar(metaEjemplo);

        // Then
        assertThat(resultado).isNotNull();
        assertThat(resultado.getId()).isEqualTo(1L);
        assertThat(resultado.getNombre()).isEqualTo("Aprender 50 palabras en inglés");
        verify(metaRepository, times(1)).save(metaEjemplo);
    }

    @Test
    @DisplayName("Debería obtener una meta por ID cuando existe")
    void testObtenerCuandoExiste() {
        // Given
        when(metaRepository.findById(1L)).thenReturn(Optional.of(metaEjemplo));

        // When - CORREGIDO: retorna Meta, no Optional
        Meta resultado = metaService.obtener(1L);

        // Then
        assertThat(resultado).isNotNull();
        assertThat(resultado.getNombre()).isEqualTo("Aprender 50 palabras en inglés");
        verify(metaRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Debería retornar Optional vacío cuando la meta no existe")
    void testObtenerCuandoNoExiste() {
        // Given
        when(metaRepository.findById(99L)).thenReturn(Optional.empty());

        // When & Then - CORREGIDO: espera excepción
        assertThrows(ResourceNotFoundException.class, () -> {
            metaService.obtener(99L);
        });

        verify(metaRepository, times(1)).findById(99L);
    }

    @Test
    @DisplayName("Debería eliminar una meta por ID")
    void testEliminar() {
        // Given - CORREGIDO: añadir mock para existsById
        when(metaRepository.existsById(1L)).thenReturn(true);
        doNothing().when(metaRepository).deleteById(1L);

        // When
        metaService.eliminar(1L);

        // Then
        verify(metaRepository, times(1)).existsById(1L);
        verify(metaRepository, times(1)).deleteById(1L);
    }
}