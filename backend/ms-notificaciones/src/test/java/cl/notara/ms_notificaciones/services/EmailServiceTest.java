package cl.notara.ms_notificaciones.services;

import cl.notara.ms_notificaciones.dto.SuscripcionEventDTO;
import jakarta.mail.Address;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
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

    private SuscripcionEventDTO crearEvento(String tipoEvento) {
        SuscripcionEventDTO e = new SuscripcionEventDTO();
        e.setIdSuscripcion(1L);
        e.setIdUsuario(10L);
        e.setEmailDestinatario("usuario@ejemplo.com");
        e.setNombreUsuario("Juan Perez");
        e.setPlan("BASICO");
        e.setEstado("ACTIVA");
        e.setMonto(9990.0);
        e.setFechaInicio(LocalDate.of(2025, 1, 1));
        e.setFechaFin(LocalDate.of(2025, 12, 31));
        e.setTipoEvento(tipoEvento);
        return e;
    }

    private MimeMessage mimeMessageReal() {
        return new MimeMessage(Session.getInstance(new Properties()));
    }

    @Test
    void enviarNotificacion_suscripcionCreada_enviaEmail() {
        when(mailSender.createMimeMessage()).thenReturn(mimeMessageReal());

        emailService.enviarNotificacion(crearEvento("SUSCRIPCION_CREADA"));

        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    void enviarNotificacion_suscripcionCancelada_enviaEmail() {
        when(mailSender.createMimeMessage()).thenReturn(mimeMessageReal());

        emailService.enviarNotificacion(crearEvento("SUSCRIPCION_CANCELADA"));

        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    void enviarNotificacion_suscripcionRenovada_enviaEmail() {
        when(mailSender.createMimeMessage()).thenReturn(mimeMessageReal());

        emailService.enviarNotificacion(crearEvento("SUSCRIPCION_RENOVADA"));

        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    void enviarNotificacion_tipoDesconocido_enviaEmailConAsuntoDefault() {
        when(mailSender.createMimeMessage()).thenReturn(mimeMessageReal());

        emailService.enviarNotificacion(crearEvento("TIPO_DESCONOCIDO"));

        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    void enviarNotificacion_montoNull_construyeEmailSinError() {
        when(mailSender.createMimeMessage()).thenReturn(mimeMessageReal());
        SuscripcionEventDTO evento = crearEvento("SUSCRIPCION_CREADA");
        evento.setMonto(null);

        emailService.enviarNotificacion(evento);

        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    void enviarNotificacion_errorMensajeria_lanzaRuntimeException() throws MessagingException {
        MimeMessage mockMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mockMessage);
        doThrow(new MessagingException("Error SMTP")).when(mockMessage).setFrom(any(Address.class));

        assertThatThrownBy(() -> emailService.enviarNotificacion(crearEvento("SUSCRIPCION_CREADA")))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Error al enviar email");
    }
}
