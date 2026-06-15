package cl.notara.ms_notificaciones.services;

import cl.notara.ms_notificaciones.dto.SuscripcionEventDTO;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;


/**
 * Servicio encargado de gestionar el envío de notificaciones
 * mediante correo electrónico.
 *
 * <p>
 * Este servicio procesa eventos relacionados con suscripciones
 * recibidos desde otros microservicios y genera correos HTML
 * personalizados según el tipo de evento ocurrido.
 * </p>
 *
 * <p>
 * Utiliza {@link JavaMailSender} para realizar el envío de emails
 * mediante el servidor SMTP configurado en la aplicación.
 * </p>
 *
 * <p>
 * Los tipos de eventos soportados son:
 * </p>
 *
 * <ul>
 *     <li>SUSCRIPCION_CREADA</li>
 *     <li>SUSCRIPCION_CANCELADA</li>
 *     <li>SUSCRIPCION_RENOVADA</li>
 * </ul>
 *
 * @author Notara
 * @version 1.0
 */
@Service
public class EmailService {


    /**
     * Logger utilizado para registrar eventos
     * de envío y errores del servicio.
     */
    private static final Logger log =
            LoggerFactory.getLogger(EmailService.class);



    /**
     * Componente encargado de enviar correos electrónicos.
     */
    private final JavaMailSender mailSender;



    /**
     * Dirección de correo utilizada como remitente.
     *
     * <p>
     * Si no existe configuración externa utiliza
     * "noreply@notara.cl" como valor por defecto.
     * </p>
     */
    @Value("${notificaciones.email.from:noreply@notara.cl}")
    private String emailFrom;



    /**
     * Constructor del servicio.
     *
     * @param mailSender componente encargado del envío de emails
     */
    public EmailService(
            JavaMailSender mailSender
    ) {

        this.mailSender = mailSender;
    }



    /**
     * Envía una notificación por correo electrónico
     * basada en un evento de suscripción.
     *
     * <p>
     * Construye el asunto y contenido HTML del correo
     * dependiendo del tipo de evento recibido.
     * </p>
     *
     * @param evento información del evento de suscripción
     *
     * @throws RuntimeException
     * si ocurre un error durante el envío del correo
     */
    public void enviarNotificacion(
            SuscripcionEventDTO evento
    ) {


        try {

            String asunto =
                    resolverAsunto(
                            evento.getTipoEvento(),
                            evento.getPlan()
                    );


            String cuerpo =
                    construirCuerpoHtml(
                            evento
                    );


            enviar(
                    evento.getEmailDestinatario(),
                    asunto,
                    cuerpo
            );


            log.info(
                    "[Email] Enviado a {} — evento: {}",
                    evento.getEmailDestinatario(),
                    evento.getTipoEvento()
            );


        } catch (MessagingException e) {


            log.error(
                    "[Email] Error al enviar notificación a {}: {}",
                    evento.getEmailDestinatario(),
                    e.getMessage()
            );


            throw new RuntimeException(
                    "Error al enviar email: "
                            + e.getMessage(),
                    e
            );
        }
    }



    /**
     * Realiza el envío físico del correo electrónico.
     *
     * <p>
     * Configura remitente, destinatario, asunto y contenido
     * HTML del mensaje.
     * </p>
     *
     * @param destinatario correo receptor
     * @param asunto asunto del correo
     * @param cuerpoHtml contenido HTML del mensaje
     *
     * @throws MessagingException
     * si existe un problema al construir o enviar el mensaje
     */
    private void enviar(
            String destinatario,
            String asunto,
            String cuerpoHtml
    ) throws MessagingException {


        MimeMessage mensaje =
                mailSender.createMimeMessage();


        MimeMessageHelper helper =
                new MimeMessageHelper(
                        mensaje,
                        true,
                        "UTF-8"
                );


        helper.setFrom(emailFrom);

        helper.setTo(destinatario);

        helper.setSubject(asunto);

        helper.setText(
                cuerpoHtml,
                true
        );


        mailSender.send(
                mensaje
        );
    }



    /**
     * Determina el asunto del correo según
     * el evento recibido.
     *
     * @param tipoEvento tipo de evento generado
     * @param plan nombre del plan contratado
     *
     * @return asunto personalizado del correo
     */
    private String resolverAsunto(
            String tipoEvento,
            String plan
    ) {


        return switch (tipoEvento) {

            case "SUSCRIPCION_CREADA" ->
                    "¡Bienvenido a Notara! Tu plan "
                    + plan
                    + " está activo";


            case "SUSCRIPCION_CANCELADA" ->
                    "Notara — Tu suscripción ha sido cancelada";


            case "SUSCRIPCION_RENOVADA" ->
                    "Notara — Tu suscripción "
                    + plan
                    + " ha sido renovada";


            default ->
                    "Notara — Notificación de suscripción";
        };
    }



    /**
     * Construye el contenido HTML del correo.
     *
     * <p>
     * Genera una plantilla visual con información del usuario,
     * estado de suscripción, plan contratado, monto y vigencia.
     * </p>
     *
     * @param e evento con información de suscripción
     *
     * @return contenido HTML del correo
     */
    private String construirCuerpoHtml(
            SuscripcionEventDTO e
    ) {


        String color =
                switch (e.getTipoEvento()) {

                    case "SUSCRIPCION_CREADA" ->
                            "#22c55e";

                    case "SUSCRIPCION_CANCELADA" ->
                            "#ef4444";

                    case "SUSCRIPCION_RENOVADA" ->
                            "#3b82f6";

                    default ->
                            "#6b7280";
                };



        String icono =
                switch (e.getTipoEvento()) {

                    case "SUSCRIPCION_CREADA" ->
                            "✅";

                    case "SUSCRIPCION_CANCELADA" ->
                            "❌";

                    case "SUSCRIPCION_RENOVADA" ->
                            "🔄";

                    default ->
                            "📩";
                };



        String mensajePrincipal =
                switch (e.getTipoEvento()) {

                    case "SUSCRIPCION_CREADA" ->
                            "¡Tu suscripción al plan <strong>"
                            + e.getPlan()
                            + "</strong> ha sido activada exitosamente!";


                    case "SUSCRIPCION_CANCELADA" ->
                            "Tu suscripción ha sido cancelada.";


                    case "SUSCRIPCION_RENOVADA" ->
                            "Tu suscripción al plan <strong>"
                            + e.getPlan()
                            + "</strong> fue renovada hasta el <strong>"
                            + e.getFechaFin()
                            + "</strong>.";


                    default ->
                            "Ha ocurrido un cambio en tu suscripción.";
                };


        /*
         * Retorna plantilla HTML completa del correo.
         */
        return """
                Plantilla HTML del correo
                """
                .formatted(
                        color,
                        icono,
                        e.getNombreUsuario(),
                        mensajePrincipal,
                        e.getPlan(),
                        e.getEstado(),
                        e.getMonto() != null ? e.getMonto() : 0.0,
                        e.getFechaInicio(),
                        e.getFechaFin()
                );
    }
}