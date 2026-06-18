package cl.notara.ms_notificaciones.listener;

import cl.notara.ms_notificaciones.config.RabbitMQConfig;
import cl.notara.ms_notificaciones.dto.SuscripcionEventDTO;
import cl.notara.ms_notificaciones.services.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Listener de mensajes RabbitMQ para el procesamiento de eventos
 * de suscripción.
 *
 * <p>
 * Escucha la cola configurada en {@link RabbitMQConfig} y reacciona
 * ante eventos publicados por el microservicio de pagos y suscripciones.
 * Tipos de eventos soportados: SUSCRIPCION_CREADA, SUSCRIPCION_CANCELADA,
 * SUSCRIPCION_RENOVADA.
 * </p>
 *
 * @author Notara
 * @version 1.0
 */
@Component
public class NotificacionListener {

    /** Logger para registrar eventos y advertencias del listener. */
    private static final Logger log = LoggerFactory.getLogger(NotificacionListener.class);

    /** Servicio encargado del envío de correos electrónicos. */
    private final EmailService emailService;

    /**
     * Constructor del listener.
     *
     * @param emailService servicio de envío de emails
     */
    public NotificacionListener(EmailService emailService) {
        this.emailService = emailService;
    }

    /**
     * Procesa un evento de suscripción recibido desde RabbitMQ.
     *
     * <p>
     * Valida que el evento contenga un email destinatario antes de
     * intentar enviar la notificación. Si el campo está vacío o nulo,
     * el evento se descarta con una advertencia en el log.
     * </p>
     *
     * @param evento datos del evento de suscripción recibido
     */
    @RabbitListener(queues = RabbitMQConfig.QUEUE)
    public void procesarEvento(SuscripcionEventDTO evento) {
        log.info("[RabbitMQ] Evento recibido: {} para usuario {} ({})",
                evento.getTipoEvento(), evento.getNombreUsuario(), evento.getEmailDestinatario());

        if (evento.getEmailDestinatario() == null || evento.getEmailDestinatario().isBlank()) {
            log.warn("[RabbitMQ] Evento sin email destinatario — descartado: {}", evento.getTipoEvento());
            return;
        }

        emailService.enviarNotificacion(evento);
    }
}
