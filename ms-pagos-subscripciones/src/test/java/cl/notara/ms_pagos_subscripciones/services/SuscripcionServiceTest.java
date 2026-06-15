package cl.notara.ms_pagos_subscripciones.services;

import cl.notara.ms_pagos_subscripciones.config.RabbitMQConfig;
import cl.notara.ms_pagos_subscripciones.dto.SuscripcionEventDTO;
import cl.notara.ms_pagos_subscripciones.exceptions.ResourceNotFoundException;
import cl.notara.ms_pagos_subscripciones.models.EstadoSuscripcion;
import cl.notara.ms_pagos_subscripciones.models.Plan;
import cl.notara.ms_pagos_subscripciones.models.Suscripcion;
import cl.notara.ms_pagos_subscripciones.repositories.SuscripcionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.AmqpTemplate;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SuscripcionServiceTest {

    @Mock
    private SuscripcionRepository repository;

    @Mock
    private AmqpTemplate amqpTemplate;

    @InjectMocks
    private SuscripcionService service;

    private Suscripcion suscripcion;

    @BeforeEach
    void setUp() {
        suscripcion = new Suscripcion();
        suscripcion.setId(1L);
        suscripcion.setIdUsuario(10L);
        suscripcion.setEmailUsuario("test@example.com");
        suscripcion.setNombreUsuario("Juan Perez");
        suscripcion.setPlan(Plan.BASICO);
        suscripcion.setEstado(EstadoSuscripcion.ACTIVA);
        suscripcion.setFechaInicio(LocalDate.now());
        suscripcion.setFechaFin(LocalDate.now().plusMonths(1));
        suscripcion.setMonto(9990.0);
    }

    @Test
    void listar_retornaListaCompleta() {
        when(repository.findAll()).thenReturn(Arrays.asList(suscripcion));

        List<Suscripcion> resultado = service.listar();

        assertThat(resultado).hasSize(1).contains(suscripcion);
        verify(repository).findAll();
    }

    @Test
    void listar_retornaListaVacia() {
        when(repository.findAll()).thenReturn(Collections.emptyList());

        List<Suscripcion> resultado = service.listar();

        assertThat(resultado).isEmpty();
    }

    @Test
    void listarPorUsuario_retornaListaFiltradaPorUsuario() {
        when(repository.findByIdUsuario(10L)).thenReturn(Arrays.asList(suscripcion));

        List<Suscripcion> resultado = service.listarPorUsuario(10L);

        assertThat(resultado).hasSize(1);
        verify(repository).findByIdUsuario(10L);
    }

    @Test
    void listarPorUsuario_usuarioSinSuscripciones_retornaVacio() {
        when(repository.findByIdUsuario(99L)).thenReturn(Collections.emptyList());

        List<Suscripcion> resultado = service.listarPorUsuario(99L);

        assertThat(resultado).isEmpty();
    }

    @Test
    void obtener_cuandoExiste_retornaSuscripcion() {
        when(repository.findById(1L)).thenReturn(Optional.of(suscripcion));

        Suscripcion resultado = service.obtener(1L);

        assertThat(resultado).isEqualTo(suscripcion);
    }

    @Test
    void obtener_cuandoNoExiste_lanzaResourceNotFoundException() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.obtener(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void crear_sinSuscripcionActiva_guardaYPublicaEvento() {
        Suscripcion nueva = new Suscripcion();
        nueva.setIdUsuario(20L);
        nueva.setEmailUsuario("nuevo@example.com");
        nueva.setNombreUsuario("Maria Lopez");
        nueva.setPlan(Plan.PREMIUM);
        nueva.setFechaInicio(LocalDate.now());
        nueva.setFechaFin(LocalDate.now().plusMonths(6));
        nueva.setMonto(19990.0);

        when(repository.findByIdUsuarioAndEstado(20L, EstadoSuscripcion.ACTIVA))
                .thenReturn(Optional.empty());
        when(repository.save(any(Suscripcion.class))).thenAnswer(inv -> inv.getArgument(0));

        Suscripcion resultado = service.crear(nueva);

        assertThat(resultado.getEstado()).isEqualTo(EstadoSuscripcion.ACTIVA);
        verify(repository).save(nueva);
        verify(amqpTemplate).convertAndSend(
                eq(RabbitMQConfig.EXCHANGE),
                eq(RabbitMQConfig.RK_CREADA),
                any(SuscripcionEventDTO.class));
    }

    @Test
    void crear_conSuscripcionActivaExistente_lanzaIllegalStateException() {
        when(repository.findByIdUsuarioAndEstado(10L, EstadoSuscripcion.ACTIVA))
                .thenReturn(Optional.of(suscripcion));

        Suscripcion nueva = new Suscripcion();
        nueva.setIdUsuario(10L);

        assertThatThrownBy(() -> service.crear(nueva))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("activa");

        verify(repository, never()).save(any());
        verifyNoInteractions(amqpTemplate);
    }

    @Test
    void cancelar_suscripcionActiva_cambiaEstadoYPublicaEvento() {
        when(repository.findById(1L)).thenReturn(Optional.of(suscripcion));
        when(repository.save(suscripcion)).thenReturn(suscripcion);

        Suscripcion resultado = service.cancelar(1L);

        assertThat(resultado.getEstado()).isEqualTo(EstadoSuscripcion.CANCELADA);
        verify(amqpTemplate).convertAndSend(
                eq(RabbitMQConfig.EXCHANGE),
                eq(RabbitMQConfig.RK_CANCELADA),
                any(SuscripcionEventDTO.class));
    }

    @Test
    void cancelar_suscripcionYaCancelada_lanzaIllegalStateException() {
        suscripcion.setEstado(EstadoSuscripcion.CANCELADA);
        when(repository.findById(1L)).thenReturn(Optional.of(suscripcion));

        assertThatThrownBy(() -> service.cancelar(1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cancelada");

        verify(repository, never()).save(any());
    }

    @Test
    void cancelar_suscripcionNoExiste_lanzaResourceNotFoundException() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.cancelar(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void renovar_suscripcionActiva_actualizaFechaYPublicaEvento() {
        LocalDate nuevaFecha = LocalDate.now().plusMonths(3);
        when(repository.findById(1L)).thenReturn(Optional.of(suscripcion));
        when(repository.save(suscripcion)).thenReturn(suscripcion);

        Suscripcion resultado = service.renovar(1L, nuevaFecha);

        assertThat(resultado.getFechaFin()).isEqualTo(nuevaFecha);
        assertThat(resultado.getEstado()).isEqualTo(EstadoSuscripcion.ACTIVA);
        verify(amqpTemplate).convertAndSend(
                eq(RabbitMQConfig.EXCHANGE),
                eq(RabbitMQConfig.RK_RENOVADA),
                any(SuscripcionEventDTO.class));
    }

    @Test
    void renovar_suscripcionVencida_actualizaEstadoAActiva() {
        suscripcion.setEstado(EstadoSuscripcion.VENCIDA);
        LocalDate nuevaFecha = LocalDate.now().plusMonths(2);
        when(repository.findById(1L)).thenReturn(Optional.of(suscripcion));
        when(repository.save(suscripcion)).thenReturn(suscripcion);

        Suscripcion resultado = service.renovar(1L, nuevaFecha);

        assertThat(resultado.getEstado()).isEqualTo(EstadoSuscripcion.ACTIVA);
    }

    @Test
    void renovar_suscripcionCancelada_lanzaIllegalStateException() {
        suscripcion.setEstado(EstadoSuscripcion.CANCELADA);
        when(repository.findById(1L)).thenReturn(Optional.of(suscripcion));

        assertThatThrownBy(() -> service.renovar(1L, LocalDate.now().plusMonths(1)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cancelada");

        verify(repository, never()).save(any());
    }

    @Test
    void renovar_suscripcionNoExiste_lanzaResourceNotFoundException() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.renovar(99L, LocalDate.now().plusMonths(1)))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
