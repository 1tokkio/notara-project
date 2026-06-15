package cl.notara.ms_notificaciones.listener;

import cl.notara.ms_notificaciones.dto.SuscripcionEventDTO;
import cl.notara.ms_notificaciones.services.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
        evento.setNombreUsuario("Juan Perez");
        evento.setEmailDestinatario("usuario@ejemplo.com");
    }

    @Test
    void procesarEvento_emailValido_llamaEnviarNotificacion() {
        listener.procesarEvento(evento);

        verify(emailService).enviarNotificacion(evento);
    }

    @Test
    void procesarEvento_emailNull_noLlamaEnviarNotificacion() {
        evento.setEmailDestinatario(null);

        listener.procesarEvento(evento);

        verifyNoInteractions(emailService);
    }

    @Test
    void procesarEvento_emailBlanco_noLlamaEnviarNotificacion() {
        evento.setEmailDestinatario("   ");

        listener.procesarEvento(evento);

        verifyNoInteractions(emailService);
    }

    @Test
    void procesarEvento_emailVacio_noLlamaEnviarNotificacion() {
        evento.setEmailDestinatario("");

        listener.procesarEvento(evento);

        verifyNoInteractions(emailService);
    }
}
