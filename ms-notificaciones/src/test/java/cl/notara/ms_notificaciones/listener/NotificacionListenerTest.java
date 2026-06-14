package cl.notara.ms_notificaciones.listener;

import cl.notara.ms_notificaciones.dto.SuscripcionEventDTO;
import cl.notara.ms_notificaciones.services.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificacionListenerTest {

    @Mock
    private EmailService emailService;

    @InjectMocks
    private NotificacionListener listener;

    private SuscripcionEventDTO evento;

    @BeforeEach
    void setUp() {
        evento = new SuscripcionEventDTO();
        evento.setTipoEvento("SUSCRIPCION_CREADA");
        evento.setNombreUsuario("Juan Pérez");
        evento.setPlan("PREMIUM");
        evento.setEstado("ACTIVA");
        evento.setMonto(9990.0);
        evento.setFechaInicio(LocalDate.of(2024, 1, 1));
        evento.setFechaFin(LocalDate.of(2024, 2, 1));
    }

    @Test
    @DisplayName("procesarEvento() - email válido → delega a EmailService")
    void procesarEvento_emailValido_delegaAEmailService() {
        evento.setEmailDestinatario("usuario@test.cl");

        listener.procesarEvento(evento);

        verify(emailService).enviarNotificacion(evento);
    }

    @Test
    @DisplayName("procesarEvento() - email null → descarta sin llamar EmailService")
    void procesarEvento_emailNulo_noDelega() {
        evento.setEmailDestinatario(null);

        listener.procesarEvento(evento);

        verify(emailService, never()).enviarNotificacion(any());
    }

    @Test
    @DisplayName("procesarEvento() - email string vacío → descarta sin llamar EmailService")
    void procesarEvento_emailVacio_noDelega() {
        evento.setEmailDestinatario("");

        listener.procesarEvento(evento);

        verify(emailService, never()).enviarNotificacion(any());
    }

    @Test
    @DisplayName("procesarEvento() - email solo espacios → descarta sin llamar EmailService")
    void procesarEvento_emailSoloEspacios_noDelega() {
        evento.setEmailDestinatario("   ");

        listener.procesarEvento(evento);

        verify(emailService, never()).enviarNotificacion(any());
    }

    @Test
    @DisplayName("procesarEvento() - evento SUSCRIPCION_CANCELADA con email válido → delega")
    void procesarEvento_cancelada_emailValido_delega() {
        evento.setTipoEvento("SUSCRIPCION_CANCELADA");
        evento.setEmailDestinatario("cancel@test.cl");

        listener.procesarEvento(evento);

        verify(emailService).enviarNotificacion(evento);
    }

    @Test
    @DisplayName("procesarEvento() - evento SUSCRIPCION_RENOVADA con email válido → delega")
    void procesarEvento_renovada_emailValido_delega() {
        evento.setTipoEvento("SUSCRIPCION_RENOVADA");
        evento.setEmailDestinatario("renew@test.cl");

        listener.procesarEvento(evento);

        verify(emailService).enviarNotificacion(evento);
    }
}
