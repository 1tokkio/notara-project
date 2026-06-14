package cl.notara.ms_notificaciones.services;

import cl.notara.ms_notificaciones.dto.SuscripcionEventDTO;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailService emailService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(emailService, "emailFrom", "noreply@notara.cl");
    }

    private MimeMessage nuevaMimeMessage() {
        return new MimeMessage(Session.getInstance(new Properties()));
    }

    private SuscripcionEventDTO buildEvento(String tipoEvento) {
        SuscripcionEventDTO e = new SuscripcionEventDTO();
        e.setTipoEvento(tipoEvento);
        e.setEmailDestinatario("usuario@test.cl");
        e.setNombreUsuario("Juan Pérez");
        e.setPlan("PREMIUM");
        e.setEstado("ACTIVA");
        e.setMonto(9990.0);
        e.setFechaInicio(LocalDate.of(2024, 1, 1));
        e.setFechaFin(LocalDate.of(2024, 2, 1));
        return e;
    }

    @Test
    @DisplayName("enviarNotificacion() - SUSCRIPCION_CREADA → envía email con asunto de bienvenida")
    void enviarNotificacion_creada_enviaEmail() {
        when(mailSender.createMimeMessage()).thenReturn(nuevaMimeMessage());

        emailService.enviarNotificacion(buildEvento("SUSCRIPCION_CREADA"));

        verify(mailSender).send(any(MimeMessage.class));
        verify(mailSender).createMimeMessage();
    }

    @Test
    @DisplayName("enviarNotificacion() - SUSCRIPCION_CANCELADA → envía email de cancelación")
    void enviarNotificacion_cancelada_enviaEmail() {
        when(mailSender.createMimeMessage()).thenReturn(nuevaMimeMessage());

        emailService.enviarNotificacion(buildEvento("SUSCRIPCION_CANCELADA"));

        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    @DisplayName("enviarNotificacion() - SUSCRIPCION_RENOVADA → envía email de renovación")
    void enviarNotificacion_renovada_enviaEmail() {
        when(mailSender.createMimeMessage()).thenReturn(nuevaMimeMessage());

        emailService.enviarNotificacion(buildEvento("SUSCRIPCION_RENOVADA"));

        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    @DisplayName("enviarNotificacion() - tipo desconocido → envía email con asunto genérico")
    void enviarNotificacion_tipoDesconocido_enviaEmailGenerico() {
        when(mailSender.createMimeMessage()).thenReturn(nuevaMimeMessage());

        emailService.enviarNotificacion(buildEvento("EVENTO_DESCONOCIDO"));

        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    @DisplayName("enviarNotificacion() - monto null → usa 0.0 como fallback, no lanza excepción")
    void enviarNotificacion_montoNull_enviaEmailSinError() {
        SuscripcionEventDTO evento = buildEvento("SUSCRIPCION_CREADA");
        evento.setMonto(null);
        when(mailSender.createMimeMessage()).thenReturn(nuevaMimeMessage());

        emailService.enviarNotificacion(evento);

        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    @DisplayName("enviarNotificacion() - SUSCRIPCION_CREADA plan BASICO → envía email")
    void enviarNotificacion_planBasico_enviaEmail() {
        SuscripcionEventDTO evento = buildEvento("SUSCRIPCION_CREADA");
        evento.setPlan("BASICO");
        evento.setMonto(4990.0);
        when(mailSender.createMimeMessage()).thenReturn(nuevaMimeMessage());

        emailService.enviarNotificacion(evento);

        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    @DisplayName("enviarNotificacion() - SUSCRIPCION_RENOVADA sin fechaFin → no lanza excepción")
    void enviarNotificacion_renovadaSinFechaFin_enviaEmail() {
        SuscripcionEventDTO evento = buildEvento("SUSCRIPCION_RENOVADA");
        evento.setFechaFin(null);
        when(mailSender.createMimeMessage()).thenReturn(nuevaMimeMessage());

        emailService.enviarNotificacion(evento);

        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    @DisplayName("enviarNotificacion() - error al enviar → lanza RuntimeException")
    void enviarNotificacion_errorAlEnviar_lanzaRuntimeException() {
        SuscripcionEventDTO evento = buildEvento("SUSCRIPCION_CREADA");
        when(mailSender.createMimeMessage()).thenReturn(nuevaMimeMessage());
        doThrow(new RuntimeException("SMTP error")).when(mailSender).send(any(MimeMessage.class));

        assertThrows(RuntimeException.class, () -> emailService.enviarNotificacion(evento));
    }
}
