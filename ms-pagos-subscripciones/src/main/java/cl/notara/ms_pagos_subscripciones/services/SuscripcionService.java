package cl.notara.ms_pagos_subscripciones.services;

import cl.notara.ms_pagos_subscripciones.config.RabbitMQConfig;
import cl.notara.ms_pagos_subscripciones.dto.SuscripcionEventDTO;
import cl.notara.ms_pagos_subscripciones.exceptions.ResourceNotFoundException;
import cl.notara.ms_pagos_subscripciones.models.EstadoSuscripcion;
import cl.notara.ms_pagos_subscripciones.models.Suscripcion;
import cl.notara.ms_pagos_subscripciones.repositories.SuscripcionRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class SuscripcionService {

    private final SuscripcionRepository repository;
    private final RabbitTemplate rabbitTemplate;

    public SuscripcionService(SuscripcionRepository repository, RabbitTemplate rabbitTemplate) {
        this.repository     = repository;
        this.rabbitTemplate = rabbitTemplate;
    }

    public List<Suscripcion> listar() {
        return repository.findAll();
    }

    public List<Suscripcion> listarPorUsuario(Long idUsuario) {
        return repository.findByIdUsuario(idUsuario);
    }

    public Suscripcion obtener(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Suscripción no encontrada con id: " + id));
    }

    public Suscripcion crear(Suscripcion suscripcion) {
        boolean tieneActiva = repository
                .findByIdUsuarioAndEstado(suscripcion.getIdUsuario(), EstadoSuscripcion.ACTIVA)
                .isPresent();
        if (tieneActiva) {
            throw new IllegalStateException("El usuario ya tiene una suscripción activa");
        }

        suscripcion.setEstado(EstadoSuscripcion.ACTIVA);
        Suscripcion guardada = repository.save(suscripcion);

        publicarEvento(guardada, RabbitMQConfig.RK_CREADA, "SUSCRIPCION_CREADA");
        return guardada;
    }

    public Suscripcion cancelar(Long id) {
        Suscripcion suscripcion = obtener(id);
        if (suscripcion.getEstado() == EstadoSuscripcion.CANCELADA) {
            throw new IllegalStateException("La suscripción ya está cancelada");
        }
        suscripcion.setEstado(EstadoSuscripcion.CANCELADA);
        Suscripcion actualizada = repository.save(suscripcion);

        publicarEvento(actualizada, RabbitMQConfig.RK_CANCELADA, "SUSCRIPCION_CANCELADA");
        return actualizada;
    }

    public Suscripcion renovar(Long id, LocalDate nuevaFechaFin) {
        Suscripcion suscripcion = obtener(id);
        if (suscripcion.getEstado() == EstadoSuscripcion.CANCELADA) {
            throw new IllegalStateException("No se puede renovar una suscripción cancelada");
        }
        suscripcion.setFechaFin(nuevaFechaFin);
        suscripcion.setEstado(EstadoSuscripcion.ACTIVA);
        Suscripcion actualizada = repository.save(suscripcion);

        publicarEvento(actualizada, RabbitMQConfig.RK_RENOVADA, "SUSCRIPCION_RENOVADA");
        return actualizada;
    }

    private void publicarEvento(Suscripcion s, String routingKey, String tipoEvento) {
        SuscripcionEventDTO evento = new SuscripcionEventDTO(
                s.getId(),
                s.getIdUsuario(),
                s.getEmailUsuario(),
                s.getNombreUsuario(),
                s.getPlan().name(),
                s.getEstado().name(),
                tipoEvento,
                s.getMonto(),
                s.getFechaInicio(),
                s.getFechaFin()
        );
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, routingKey, evento);
    }
}
