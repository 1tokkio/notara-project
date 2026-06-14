package cl.notara.ms_pagos_subscripciones.services;

import cl.notara.ms_pagos_subscripciones.config.RabbitMQConfig;
import cl.notara.ms_pagos_subscripciones.dto.SuscripcionEventDTO;
import cl.notara.ms_pagos_subscripciones.exceptions.ResourceNotFoundException;
import cl.notara.ms_pagos_subscripciones.models.EstadoSuscripcion;
import cl.notara.ms_pagos_subscripciones.models.Plan;
import cl.notara.ms_pagos_subscripciones.models.Suscripcion;
import cl.notara.ms_pagos_subscripciones.repositories.SuscripcionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SuscripcionServiceTest {

    @Mock
    private SuscripcionRepository repository;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private SuscripcionService service;

    private Suscripcion suscripcion;

    @BeforeEach
    void setUp() {
        suscripcion = new Suscripcion();
        suscripcion.setId(1L);
        suscripcion.setIdUsuario(10L);
        suscripcion.setEmailUsuario("usuario@test.cl");
        suscripcion.setNombreUsuario("Juan Pérez");
        suscripcion.setPlan(Plan.PREMIUM);
        suscripcion.setEstado(EstadoSuscripcion.ACTIVA);
        suscripcion.setFechaInicio(LocalDate.of(2024, 1, 1));
        suscripcion.setFechaFin(LocalDate.of(2024, 2, 1));
        suscripcion.setMonto(9990.0);
    }

    // ─── listar() ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("listar() - retorna todas las suscripciones")
    void listar_retornaLista() {
        when(repository.findAll()).thenReturn(List.of(suscripcion));

        List<Suscripcion> resultado = service.listar();

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getEmailUsuario()).isEqualTo("usuario@test.cl");
        verify(repository).findAll();
    }

    @Test
    @DisplayName("listar() - retorna lista vacía cuando no hay suscripciones")
    void listar_listaVacia() {
        when(repository.findAll()).thenReturn(List.of());

        assertThat(service.listar()).isEmpty();
        verify(repository).findAll();
    }

    // ─── listarPorUsuario() ───────────────────────────────────────────────────

    @Test
    @DisplayName("listarPorUsuario() - retorna suscripciones del usuario")
    void listarPorUsuario_retornaLista() {
        when(repository.findByIdUsuario(10L)).thenReturn(List.of(suscripcion));

        List<Suscripcion> resultado = service.listarPorUsuario(10L);

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getIdUsuario()).isEqualTo(10L);
        verify(repository).findByIdUsuario(10L);
    }

    @Test
    @DisplayName("listarPorUsuario() - usuario sin suscripciones retorna lista vacía")
    void listarPorUsuario_sinSuscripciones_listaVacia() {
        when(repository.findByIdUsuario(99L)).thenReturn(List.of());

        assertThat(service.listarPorUsuario(99L)).isEmpty();
    }

    // ─── obtener() ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("obtener() - suscripción encontrada retorna objeto")
    void obtener_existente_retornaSuscripcion() {
        when(repository.findById(1L)).thenReturn(Optional.of(suscripcion));

        Suscripcion resultado = service.obtener(1L);

        assertThat(resultado.getId()).isEqualTo(1L);
        assertThat(resultado.getPlan()).isEqualTo(Plan.PREMIUM);
        verify(repository).findById(1L);
    }

    @Test
    @DisplayName("obtener() - suscripción no encontrada lanza ResourceNotFoundException")
    void obtener_noExistente_lanzaExcepcion() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> service.obtener(99L));
        assertThat(ex.getMessage()).contains("99");
        verify(repository).findById(99L);
    }

    // ─── crear() ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("crear() - usuario sin suscripción activa → crea y publica evento CREADA")
    void crear_sinSuscripcionActiva_creaYPublicaEvento() {
        when(repository.findByIdUsuarioAndEstado(10L, EstadoSuscripcion.ACTIVA))
                .thenReturn(Optional.empty());
        when(repository.save(any(Suscripcion.class))).thenReturn(suscripcion);

        Suscripcion resultado = service.crear(suscripcion);

        assertThat(resultado).isNotNull();
        assertThat(suscripcion.getEstado()).isEqualTo(EstadoSuscripcion.ACTIVA);
        verify(repository).save(suscripcion);
        verify(rabbitTemplate).convertAndSend(
                eq(RabbitMQConfig.EXCHANGE),
                eq(RabbitMQConfig.RK_CREADA),
                any(SuscripcionEventDTO.class));
    }

    @Test
    @DisplayName("crear() - usuario ya tiene suscripción activa → lanza IllegalStateException")
    void crear_conSuscripcionActiva_lanzaIllegalState() {
        when(repository.findByIdUsuarioAndEstado(10L, EstadoSuscripcion.ACTIVA))
                .thenReturn(Optional.of(suscripcion));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> service.crear(suscripcion));
        assertThat(ex.getMessage()).contains("ya tiene una suscripción activa");
        verify(repository, never()).save(any());
        verify(rabbitTemplate, never()).convertAndSend(any(), any(), any(Object.class));
    }

    // ─── cancelar() ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("cancelar() - suscripción activa → cancela y publica evento CANCELADA")
    void cancelar_activa_cancelaYPublicaEvento() {
        when(repository.findById(1L)).thenReturn(Optional.of(suscripcion));
        when(repository.save(any(Suscripcion.class))).thenReturn(suscripcion);

        Suscripcion resultado = service.cancelar(1L);

        assertThat(resultado.getEstado()).isEqualTo(EstadoSuscripcion.CANCELADA);
        verify(repository).save(suscripcion);
        verify(rabbitTemplate).convertAndSend(
                eq(RabbitMQConfig.EXCHANGE),
                eq(RabbitMQConfig.RK_CANCELADA),
                any(SuscripcionEventDTO.class));
    }

    @Test
    @DisplayName("cancelar() - suscripción ya cancelada → lanza IllegalStateException")
    void cancelar_yaCancelada_lanzaIllegalState() {
        suscripcion.setEstado(EstadoSuscripcion.CANCELADA);
        when(repository.findById(1L)).thenReturn(Optional.of(suscripcion));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> service.cancelar(1L));
        assertThat(ex.getMessage()).contains("ya está cancelada");
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("cancelar() - suscripción no existe → lanza ResourceNotFoundException")
    void cancelar_noExistente_lanzaExcepcion() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.cancelar(99L));
        verify(repository, never()).save(any());
    }

    // ─── renovar() ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("renovar() - suscripción activa → renueva y publica evento RENOVADA")
    void renovar_activa_renovaYPublicaEvento() {
        LocalDate nuevaFechaFin = LocalDate.of(2024, 3, 1);
        when(repository.findById(1L)).thenReturn(Optional.of(suscripcion));
        when(repository.save(any(Suscripcion.class))).thenReturn(suscripcion);

        Suscripcion resultado = service.renovar(1L, nuevaFechaFin);

        assertThat(resultado.getEstado()).isEqualTo(EstadoSuscripcion.ACTIVA);
        assertThat(suscripcion.getFechaFin()).isEqualTo(nuevaFechaFin);
        verify(repository).save(suscripcion);
        verify(rabbitTemplate).convertAndSend(
                eq(RabbitMQConfig.EXCHANGE),
                eq(RabbitMQConfig.RK_RENOVADA),
                any(SuscripcionEventDTO.class));
    }

    @Test
    @DisplayName("renovar() - suscripción vencida → renueva correctamente (estado no CANCELADA)")
    void renovar_vencida_renovaCorrectamente() {
        suscripcion.setEstado(EstadoSuscripcion.VENCIDA);
        LocalDate nuevaFechaFin = LocalDate.of(2024, 4, 1);
        when(repository.findById(1L)).thenReturn(Optional.of(suscripcion));
        when(repository.save(any(Suscripcion.class))).thenReturn(suscripcion);

        Suscripcion resultado = service.renovar(1L, nuevaFechaFin);

        assertThat(resultado.getEstado()).isEqualTo(EstadoSuscripcion.ACTIVA);
        verify(rabbitTemplate).convertAndSend(eq(RabbitMQConfig.EXCHANGE),
                eq(RabbitMQConfig.RK_RENOVADA), any(SuscripcionEventDTO.class));
    }

    @Test
    @DisplayName("renovar() - suscripción cancelada → lanza IllegalStateException")
    void renovar_cancelada_lanzaIllegalState() {
        suscripcion.setEstado(EstadoSuscripcion.CANCELADA);
        when(repository.findById(1L)).thenReturn(Optional.of(suscripcion));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> service.renovar(1L, LocalDate.of(2024, 3, 1)));
        assertThat(ex.getMessage()).contains("cancelada");
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("renovar() - suscripción no existe → lanza ResourceNotFoundException")
    void renovar_noExistente_lanzaExcepcion() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> service.renovar(99L, LocalDate.of(2024, 3, 1)));
        verify(repository, never()).save(any());
    }
}
