package cl.notara.ms_notificaciones.listener;

import cl.notara.ms_notificaciones.config.RabbitMQConfig;
import cl.notara.ms_notificaciones.dto.SuscripcionEventDTO;
import cl.notara.ms_notificaciones.services.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class NotificacionListener {

    private static final Logger log = LoggerFactory.getLogger(NotificacionListener.class);

    private final EmailService emailService;

    public NotificacionListener(EmailService emailService) {
        this.emailService = emailService;
    }

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
