package cl.notara.ms_pagos_subscripciones.services;

import cl.notara.ms_pagos_subscripciones.config.RabbitMQConfig;
import cl.notara.ms_pagos_subscripciones.dto.SuscripcionEventDTO;
import cl.notara.ms_pagos_subscripciones.exceptions.ResourceNotFoundException;
import cl.notara.ms_pagos_subscripciones.models.EstadoSuscripcion;
import cl.notara.ms_pagos_subscripciones.models.Suscripcion;
import cl.notara.ms_pagos_subscripciones.repositories.SuscripcionRepository;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;


/**
 * Servicio encargado de administrar la lógica de negocio
 * relacionada con las suscripciones de usuarios.
 *
 * <p>
 * Gestiona el ciclo de vida completo de una suscripción:
 * creación, consulta, cancelación y renovación.
 * </p>
 *
 * <p>
 * Además, este servicio integra comunicación asíncrona mediante
 * RabbitMQ para publicar eventos cuando una suscripción cambia
 * de estado.
 * </p>
 *
 * <p>
 * Los eventos generados permiten que otros microservicios puedan
 * reaccionar ante acciones como creación, cancelación o renovación
 * de una suscripción.
 * </p>
 *
 * @author Notara
 * @version 1.0
 */
@Service
public class SuscripcionService {


    /**
     * Repositorio encargado de la persistencia
     * de las suscripciones.
     */
    private final SuscripcionRepository repository;


    /**
     * Cliente utilizado para enviar mensajes
     * hacia RabbitMQ.
     */
    private final AmqpTemplate amqpTemplate;



    /**
     * Constructor del servicio.
     *
     * @param repository repositorio de suscripciones
     * @param amqpTemplate componente para comunicación con RabbitMQ
     */
    public SuscripcionService(
            SuscripcionRepository repository,
            AmqpTemplate amqpTemplate
    ) {

        this.repository = repository;
        this.amqpTemplate = amqpTemplate;
    }



    /**
     * Obtiene todas las suscripciones registradas.
     *
     * @return lista completa de suscripciones
     */
    public List<Suscripcion> listar() {

        return repository.findAll();
    }



    /**
     * Obtiene las suscripciones asociadas a un usuario.
     *
     * @param idUsuario identificador del usuario
     * @return lista de suscripciones del usuario
     */
    public List<Suscripcion> listarPorUsuario(
            Long idUsuario
    ) {

        return repository.findByIdUsuario(
                idUsuario
        );
    }



    /**
     * Busca una suscripción mediante su identificador.
     *
     * @param id identificador de la suscripción
     * @return suscripción encontrada
     *
     * @throws ResourceNotFoundException
     * si la suscripción no existe
     */
    public Suscripcion obtener(
            Long id
    ) {

        return repository.findById(id)

                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Suscripción no encontrada con id: "
                                        + id
                        )
                );
    }



    /**
     * Crea una nueva suscripción para un usuario.
     *
     * <p>
     * Antes de crearla verifica que el usuario no tenga
     * otra suscripción activa.
     * </p>
     *
     * <p>
     * Una vez creada publica un evento en RabbitMQ
     * notificando la creación.
     * </p>
     *
     * @param suscripcion datos de la suscripción a crear
     * @return suscripción creada
     *
     * @throws IllegalStateException
     * si el usuario ya posee una suscripción activa
     */
    public Suscripcion crear(
            Suscripcion suscripcion
    ) {


        boolean tieneActiva =
                repository
                .findByIdUsuarioAndEstado(
                        suscripcion.getIdUsuario(),
                        EstadoSuscripcion.ACTIVA
                )
                .isPresent();


        if (tieneActiva) {

            throw new IllegalStateException(
                    "El usuario ya tiene una suscripción activa"
            );
        }


        suscripcion.setEstado(
                EstadoSuscripcion.ACTIVA
        );


        Suscripcion guardada =
                repository.save(
                        suscripcion
                );


        publicarEvento(
                guardada,
                RabbitMQConfig.RK_CREADA,
                "SUSCRIPCION_CREADA"
        );


        return guardada;
    }



    /**
     * Cancela una suscripción existente.
     *
     * @param id identificador de la suscripción
     * @return suscripción cancelada
     *
     * @throws IllegalStateException
     * si la suscripción ya estaba cancelada
     */
    public Suscripcion cancelar(
            Long id
    ) {


        Suscripcion suscripcion =
                obtener(id);



        if (
            suscripcion.getEstado()
            == EstadoSuscripcion.CANCELADA
        ) {

            throw new IllegalStateException(
                    "La suscripción ya está cancelada"
            );
        }



        suscripcion.setEstado(
                EstadoSuscripcion.CANCELADA
        );


        Suscripcion actualizada =
                repository.save(
                        suscripcion
                );



        publicarEvento(
                actualizada,
                RabbitMQConfig.RK_CANCELADA,
                "SUSCRIPCION_CANCELADA"
        );


        return actualizada;
    }



    /**
     * Renueva una suscripción modificando su fecha de término.
     *
     * <p>
     * Una suscripción cancelada no puede ser renovada.
     * </p>
     *
     * @param id identificador de suscripción
     * @param nuevaFechaFin nueva fecha de finalización
     *
     * @return suscripción renovada
     *
     * @throws IllegalStateException
     * si la suscripción está cancelada
     */
    public Suscripcion renovar(
            Long id,
            LocalDate nuevaFechaFin
    ) {


        Suscripcion suscripcion =
                obtener(id);



        if (
            suscripcion.getEstado()
            == EstadoSuscripcion.CANCELADA
        ) {

            throw new IllegalStateException(
                    "No se puede renovar una suscripción cancelada"
            );
        }



        suscripcion.setFechaFin(
                nuevaFechaFin
        );


        suscripcion.setEstado(
                EstadoSuscripcion.ACTIVA
        );



        Suscripcion actualizada =
                repository.save(
                        suscripcion
                );



        publicarEvento(
                actualizada,
                RabbitMQConfig.RK_RENOVADA,
                "SUSCRIPCION_RENOVADA"
        );



        return actualizada;
    }



    /**
     * Publica un evento de suscripción en RabbitMQ.
     *
     * <p>
     * Construye un DTO con la información relevante de la suscripción
     * y lo envía al exchange configurado utilizando la routing key
     * correspondiente.
     * </p>
     *
     * @param s suscripción modificada
     * @param routingKey clave utilizada para enrutar el mensaje
     * @param tipoEvento nombre del evento generado
     */
    private void publicarEvento(
            Suscripcion s,
            String routingKey,
            String tipoEvento
    ) {


        SuscripcionEventDTO evento =
                new SuscripcionEventDTO(

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



        amqpTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE,
                routingKey,
                evento
        );
    }
}
