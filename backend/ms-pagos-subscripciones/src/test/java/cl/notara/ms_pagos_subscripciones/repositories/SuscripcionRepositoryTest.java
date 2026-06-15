package cl.notara.ms_pagos_subscripciones.repositories;

import cl.notara.ms_pagos_subscripciones.models.EstadoSuscripcion;
import cl.notara.ms_pagos_subscripciones.models.Plan;
import cl.notara.ms_pagos_subscripciones.models.Suscripcion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class SuscripcionRepositoryTest {

    @Autowired
    private SuscripcionRepository repository;

    private Suscripcion suscripcionActiva;

    @BeforeEach
    void setUp() {
        repository.deleteAll();

        suscripcionActiva = new Suscripcion();
        suscripcionActiva.setIdUsuario(10L);
        suscripcionActiva.setEmailUsuario("activo@example.com");
        suscripcionActiva.setNombreUsuario("Juan Perez");
        suscripcionActiva.setPlan(Plan.BASICO);
        suscripcionActiva.setEstado(EstadoSuscripcion.ACTIVA);
        suscripcionActiva.setFechaInicio(LocalDate.now());
        suscripcionActiva.setFechaFin(LocalDate.now().plusMonths(1));
        suscripcionActiva.setMonto(9990.0);
        repository.save(suscripcionActiva);

        Suscripcion suscripcionCancelada = new Suscripcion();
        suscripcionCancelada.setIdUsuario(20L);
        suscripcionCancelada.setEmailUsuario("cancelado@example.com");
        suscripcionCancelada.setNombreUsuario("Pedro Garcia");
        suscripcionCancelada.setPlan(Plan.PREMIUM);
        suscripcionCancelada.setEstado(EstadoSuscripcion.CANCELADA);
        suscripcionCancelada.setFechaInicio(LocalDate.now().minusMonths(2));
        suscripcionCancelada.setFechaFin(LocalDate.now().minusMonths(1));
        suscripcionCancelada.setMonto(19990.0);
        repository.save(suscripcionCancelada);

        Suscripcion suscripcionEmpresarial = new Suscripcion();
        suscripcionEmpresarial.setIdUsuario(30L);
        suscripcionEmpresarial.setEmailUsuario("empresa@example.com");
        suscripcionEmpresarial.setNombreUsuario("Empresa SA");
        suscripcionEmpresarial.setPlan(Plan.EMPRESARIAL);
        suscripcionEmpresarial.setEstado(EstadoSuscripcion.VENCIDA);
        suscripcionEmpresarial.setFechaInicio(LocalDate.now().minusMonths(3));
        suscripcionEmpresarial.setFechaFin(LocalDate.now().minusMonths(1));
        suscripcionEmpresarial.setMonto(49990.0);
        repository.save(suscripcionEmpresarial);
    }

    @Test
    void findByIdUsuario_retornaSuscripcionesDelUsuario() {
        List<Suscripcion> resultado = repository.findByIdUsuario(10L);

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getEmailUsuario()).isEqualTo("activo@example.com");
    }

    @Test
    void findByIdUsuario_usuarioSinSuscripciones_retornaListaVacia() {
        List<Suscripcion> resultado = repository.findByIdUsuario(999L);

        assertThat(resultado).isEmpty();
    }

    @Test
    void findByEstado_activa_retornaSuscripcionesActivas() {
        List<Suscripcion> resultado = repository.findByEstado(EstadoSuscripcion.ACTIVA);

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getIdUsuario()).isEqualTo(10L);
    }

    @Test
    void findByEstado_cancelada_retornaSuscripcionesCanceladas() {
        List<Suscripcion> resultado = repository.findByEstado(EstadoSuscripcion.CANCELADA);

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getIdUsuario()).isEqualTo(20L);
    }

    @Test
    void findByEstado_pendiente_retornaListaVacia() {
        List<Suscripcion> resultado = repository.findByEstado(EstadoSuscripcion.PENDIENTE);

        assertThat(resultado).isEmpty();
    }

    @Test
    void findByIdUsuarioAndEstado_encontrado_retornaSuscripcion() {
        Optional<Suscripcion> resultado = repository.findByIdUsuarioAndEstado(10L, EstadoSuscripcion.ACTIVA);

        assertThat(resultado).isPresent();
        assertThat(resultado.get().getEmailUsuario()).isEqualTo("activo@example.com");
    }

    @Test
    void findByIdUsuarioAndEstado_estadoDistinto_retornaVacio() {
        Optional<Suscripcion> resultado = repository.findByIdUsuarioAndEstado(10L, EstadoSuscripcion.CANCELADA);

        assertThat(resultado).isEmpty();
    }

    @Test
    void findByIdUsuarioAndEstado_usuarioDistinto_retornaVacio() {
        Optional<Suscripcion> resultado = repository.findByIdUsuarioAndEstado(999L, EstadoSuscripcion.ACTIVA);

        assertThat(resultado).isEmpty();
    }

    @Test
    void save_persisteCorrectamente() {
        Suscripcion nueva = new Suscripcion();
        nueva.setIdUsuario(40L);
        nueva.setEmailUsuario("nuevo@example.com");
        nueva.setNombreUsuario("Ana Gonzalez");
        nueva.setPlan(Plan.BASICO);
        nueva.setEstado(EstadoSuscripcion.ACTIVA);
        nueva.setFechaInicio(LocalDate.now());
        nueva.setFechaFin(LocalDate.now().plusMonths(6));
        nueva.setMonto(9990.0);

        Suscripcion guardada = repository.save(nueva);

        assertThat(guardada.getId()).isNotNull();
        assertThat(guardada.getEmailUsuario()).isEqualTo("nuevo@example.com");
        assertThat(guardada.getPlan()).isEqualTo(Plan.BASICO);
    }

    @Test
    void findAll_retornaTodasLasSuscripciones() {
        List<Suscripcion> todas = repository.findAll();

        assertThat(todas).hasSize(3);
    }
}
