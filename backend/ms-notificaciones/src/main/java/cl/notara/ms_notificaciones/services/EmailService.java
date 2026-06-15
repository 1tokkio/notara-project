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

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    @Value("${notificaciones.email.from:noreply@notara.cl}")
    private String emailFrom;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void enviarNotificacion(SuscripcionEventDTO evento) {
        try {
            String asunto = resolverAsunto(evento.getTipoEvento(), evento.getPlan());
            String cuerpo = construirCuerpoHtml(evento);
            enviar(evento.getEmailDestinatario(), asunto, cuerpo);
            log.info("[Email] Enviado a {} — evento: {}", evento.getEmailDestinatario(), evento.getTipoEvento());
        } catch (MessagingException e) {
            log.error("[Email] Error al enviar notificación a {}: {}", evento.getEmailDestinatario(), e.getMessage());
            throw new RuntimeException("Error al enviar email: " + e.getMessage(), e);
        }
    }

    private void enviar(String destinatario, String asunto, String cuerpoHtml) throws MessagingException {
        MimeMessage mensaje = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mensaje, true, "UTF-8");
        helper.setFrom(emailFrom);
        helper.setTo(destinatario);
        helper.setSubject(asunto);
        helper.setText(cuerpoHtml, true);
        mailSender.send(mensaje);
    }

    private String resolverAsunto(String tipoEvento, String plan) {
        return switch (tipoEvento) {
            case "SUSCRIPCION_CREADA"   -> "¡Bienvenido a Notara! Tu plan " + plan + " está activo";
            case "SUSCRIPCION_CANCELADA" -> "Notara — Tu suscripción ha sido cancelada";
            case "SUSCRIPCION_RENOVADA"  -> "Notara — Tu suscripción " + plan + " ha sido renovada";
            default                      -> "Notara — Notificación de suscripción";
        };
    }

    private String construirCuerpoHtml(SuscripcionEventDTO e) {
        String color = switch (e.getTipoEvento()) {
            case "SUSCRIPCION_CREADA"    -> "#22c55e";
            case "SUSCRIPCION_CANCELADA" -> "#ef4444";
            case "SUSCRIPCION_RENOVADA"  -> "#3b82f6";
            default                      -> "#6b7280";
        };

        String icono = switch (e.getTipoEvento()) {
            case "SUSCRIPCION_CREADA"    -> "✅";
            case "SUSCRIPCION_CANCELADA" -> "❌";
            case "SUSCRIPCION_RENOVADA"  -> "🔄";
            default                      -> "📩";
        };

        String mensajePrincipal = switch (e.getTipoEvento()) {
            case "SUSCRIPCION_CREADA" ->
                "¡Tu suscripción al plan <strong>" + e.getPlan() + "</strong> ha sido activada exitosamente! " +
                "Disfruta de todos los beneficios de Notara.";
            case "SUSCRIPCION_CANCELADA" ->
                "Tu suscripción ha sido cancelada. Puedes volver a suscribirte en cualquier momento " +
                "desde tu cuenta en Notara.";
            case "SUSCRIPCION_RENOVADA" ->
                "Tu suscripción al plan <strong>" + e.getPlan() + "</strong> ha sido renovada " +
                "hasta el <strong>" + e.getFechaFin() + "</strong>. ¡Sigue disfrutando de Notara!";
            default -> "Ha ocurrido un cambio en tu suscripción.";
        };

        return """
                <!DOCTYPE html>
                <html lang="es">
                <head>
                  <meta charset="UTF-8">
                  <meta name="viewport" content="width=device-width, initial-scale=1.0">
                </head>
                <body style="margin:0;padding:0;background:#f3f4f6;font-family:Arial,sans-serif;">
                  <table width="100%%" cellpadding="0" cellspacing="0" style="background:#f3f4f6;padding:40px 0;">
                    <tr><td align="center">
                      <table width="600" cellpadding="0" cellspacing="0"
                             style="background:#ffffff;border-radius:12px;overflow:hidden;
                                    box-shadow:0 4px 16px rgba(0,0,0,0.08);">

                        <!-- Header -->
                        <tr>
                          <td style="background:%s;padding:32px;text-align:center;">
                            <div style="font-size:48px;">%s</div>
                            <h1 style="color:#ffffff;margin:12px 0 0;font-size:24px;font-weight:700;">Notara</h1>
                          </td>
                        </tr>

                        <!-- Body -->
                        <tr>
                          <td style="padding:40px 48px;">
                            <p style="color:#374151;font-size:16px;margin:0 0 16px;">
                              Hola, <strong>%s</strong>
                            </p>
                            <p style="color:#374151;font-size:16px;line-height:1.6;margin:0 0 32px;">
                              %s
                            </p>

                            <!-- Detalles -->
                            <table width="100%%" cellpadding="0" cellspacing="0"
                                   style="background:#f9fafb;border-radius:8px;padding:24px;">
                              <tr>
                                <td style="padding:8px 0;">
                                  <span style="color:#6b7280;font-size:14px;">Plan</span><br>
                                  <strong style="color:#111827;font-size:16px;">%s</strong>
                                </td>
                                <td style="padding:8px 0;">
                                  <span style="color:#6b7280;font-size:14px;">Estado</span><br>
                                  <strong style="color:#111827;font-size:16px;">%s</strong>
                                </td>
                              </tr>
                              <tr>
                                <td style="padding:8px 0;">
                                  <span style="color:#6b7280;font-size:14px;">Monto</span><br>
                                  <strong style="color:#111827;font-size:16px;">$%.0f CLP</strong>
                                </td>
                                <td style="padding:8px 0;">
                                  <span style="color:#6b7280;font-size:14px;">Vigencia</span><br>
                                  <strong style="color:#111827;font-size:16px;">%s → %s</strong>
                                </td>
                              </tr>
                            </table>
                          </td>
                        </tr>

                        <!-- Footer -->
                        <tr>
                          <td style="background:#f9fafb;padding:24px 48px;text-align:center;
                                     border-top:1px solid #e5e7eb;">
                            <p style="color:#9ca3af;font-size:13px;margin:0;">
                              Este correo fue enviado automáticamente por Notara.<br>
                              Si no realizaste esta acción, contáctanos de inmediato.
                            </p>
                          </td>
                        </tr>

                      </table>
                    </td></tr>
                  </table>
                </body>
                </html>
                """.formatted(
                color, icono,
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
