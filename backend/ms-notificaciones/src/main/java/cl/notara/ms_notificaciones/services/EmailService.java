package cl.notara.ms_notificaciones.services;

import cl.notara.ms_notificaciones.dto.SuscripcionEventDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * Servicio encargado de gestionar el envío de notificaciones
 * mediante correo electrónico usando la API de Resend.
 */
@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    @Value("${notificaciones.email.from:onboarding@resend.dev}")
    private String emailFrom;

    @Value("${RESEND_API_KEY:}")
    private String resendApiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    public void enviarNotificacion(SuscripcionEventDTO evento) {
        String asunto = resolverAsunto(evento.getTipoEvento(), evento.getPlan());
        String cuerpo = construirCuerpoHtml(evento);
        enviar(evento.getEmailDestinatario(), asunto, cuerpo);
        log.info("[Email] Enviado a {} — evento: {}", evento.getEmailDestinatario(), evento.getTipoEvento());
    }

    private void enviar(String destinatario, String asunto, String cuerpoHtml) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(resendApiKey);

        Map<String, Object> body = Map.of(
            "from", emailFrom,
            "to", new String[]{ destinatario },
            "subject", asunto,
            "html", cuerpoHtml
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(
            "https://api.resend.com/emails",
            request,
            String.class
        );

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Resend error: " + response.getBody());
        }

        log.info("[Resend] Respuesta: {}", response.getBody());
    }

    private String resolverAsunto(String tipoEvento, String plan) {
        return switch (tipoEvento) {
            case "SUSCRIPCION_CREADA"   -> "¡Bienvenido a Notara! Tu plan " + plan + " está activo";
            case "SUSCRIPCION_CANCELADA" -> "Notara — Tu suscripción ha sido cancelada";
            case "SUSCRIPCION_RENOVADA"  -> "Notara — Tu suscripción " + plan + " ha sido renovada";
            default -> "Notara — Notificación de suscripción";
        };
    }

    private String construirCuerpoHtml(SuscripcionEventDTO e) {
        String color = switch (e.getTipoEvento()) {
            case "SUSCRIPCION_CREADA"   -> "#22c55e";
            case "SUSCRIPCION_CANCELADA" -> "#ef4444";
            case "SUSCRIPCION_RENOVADA"  -> "#3b82f6";
            default -> "#6b7280";
        };

        String icono = switch (e.getTipoEvento()) {
            case "SUSCRIPCION_CREADA"   -> "✅";
            case "SUSCRIPCION_CANCELADA" -> "❌";
            case "SUSCRIPCION_RENOVADA"  -> "🔄";
            default -> "📩";
        };

        String mensajePrincipal = switch (e.getTipoEvento()) {
            case "SUSCRIPCION_CREADA"   -> "¡Tu suscripción al plan <strong>" + e.getPlan() + "</strong> ha sido activada exitosamente!";
            case "SUSCRIPCION_CANCELADA" -> "Tu suscripción ha sido cancelada.";
            case "SUSCRIPCION_RENOVADA"  -> "Tu suscripción al plan <strong>" + e.getPlan() + "</strong> fue renovada hasta el <strong>" + e.getFechaFin() + "</strong>.";
            default -> "Ha ocurrido un cambio en tu suscripción.";
        };

        return """
            <div style="font-family:Arial,sans-serif;max-width:600px;margin:0 auto;background:#0f0f0f;color:#fff;border-radius:12px;overflow:hidden">
              <div style="background:%s;padding:32px;text-align:center">
                <div style="font-size:48px">%s</div>
                <h1 style="margin:16px 0 0;font-size:24px">Notara</h1>
              </div>
              <div style="padding:32px">
                <p>Hola <strong>%s</strong>,</p>
                <p>%s</p>
                <table style="width:100%%;border-collapse:collapse;margin:24px 0">
                  <tr><td style="padding:8px;border-bottom:1px solid #333;color:#aaa">Plan</td><td style="padding:8px;border-bottom:1px solid #333">%s</td></tr>
                  <tr><td style="padding:8px;border-bottom:1px solid #333;color:#aaa">Estado</td><td style="padding:8px;border-bottom:1px solid #333">%s</td></tr>
                  <tr><td style="padding:8px;border-bottom:1px solid #333;color:#aaa">Monto</td><td style="padding:8px;border-bottom:1px solid #333">$%.2f USD</td></tr>
                  <tr><td style="padding:8px;border-bottom:1px solid #333;color:#aaa">Inicio</td><td style="padding:8px;border-bottom:1px solid #333">%s</td></tr>
                  <tr><td style="padding:8px;color:#aaa">Vencimiento</td><td style="padding:8px">%s</td></tr>
                </table>
                <p style="color:#aaa;font-size:12px;text-align:center">© 2025 Notara — Aprende idiomas con música</p>
              </div>
            </div>
            """.formatted(
                color, icono,
                e.getNombreUsuario(), mensajePrincipal,
                e.getPlan(), e.getEstado(),
                e.getMonto() != null ? e.getMonto() : 0.0,
                e.getFechaInicio(), e.getFechaFin()
            );
    }
}
