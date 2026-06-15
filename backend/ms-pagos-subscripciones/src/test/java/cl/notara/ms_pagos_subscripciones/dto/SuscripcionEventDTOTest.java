package cl.notara.ms_pagos_subscripciones.dto;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class SuscripcionEventDTOTest {

    @Test
    void constructorCompleto_inicializaTodosLosCampos() {
        LocalDate inicio = LocalDate.of(2025, 1, 1);
        LocalDate fin = LocalDate.of(2025, 12, 31);

        SuscripcionEventDTO dto = new SuscripcionEventDTO(
                1L, 10L, "email@test.com", "Juan Perez",
                "BASICO", "ACTIVA", "SUSCRIPCION_CREADA",
                9990.0, inicio, fin
        );

        assertThat(dto.getIdSuscripcion()).isEqualTo(1L);
        assertThat(dto.getIdUsuario()).isEqualTo(10L);
        assertThat(dto.getEmailDestinatario()).isEqualTo("email@test.com");
        assertThat(dto.getNombreUsuario()).isEqualTo("Juan Perez");
        assertThat(dto.getPlan()).isEqualTo("BASICO");
        assertThat(dto.getEstado()).isEqualTo("ACTIVA");
        assertThat(dto.getTipoEvento()).isEqualTo("SUSCRIPCION_CREADA");
        assertThat(dto.getMonto()).isEqualTo(9990.0);
        assertThat(dto.getFechaInicio()).isEqualTo(inicio);
        assertThat(dto.getFechaFin()).isEqualTo(fin);
        assertThat(dto.getFechaEvento()).isNotNull();
    }

    @Test
    void constructorVacio_permiteUsarSetters() {
        SuscripcionEventDTO dto = new SuscripcionEventDTO();
        LocalDate inicio = LocalDate.of(2025, 6, 1);
        LocalDate fin = LocalDate.of(2026, 6, 1);
        LocalDateTime ahora = LocalDateTime.now();

        dto.setIdSuscripcion(2L);
        dto.setIdUsuario(20L);
        dto.setEmailDestinatario("otro@test.com");
        dto.setNombreUsuario("Pedro Lopez");
        dto.setPlan("PREMIUM");
        dto.setEstado("CANCELADA");
        dto.setTipoEvento("SUSCRIPCION_CANCELADA");
        dto.setMonto(19990.0);
        dto.setFechaInicio(inicio);
        dto.setFechaFin(fin);
        dto.setFechaEvento(ahora);

        assertThat(dto.getIdSuscripcion()).isEqualTo(2L);
        assertThat(dto.getIdUsuario()).isEqualTo(20L);
        assertThat(dto.getEmailDestinatario()).isEqualTo("otro@test.com");
        assertThat(dto.getNombreUsuario()).isEqualTo("Pedro Lopez");
        assertThat(dto.getPlan()).isEqualTo("PREMIUM");
        assertThat(dto.getEstado()).isEqualTo("CANCELADA");
        assertThat(dto.getTipoEvento()).isEqualTo("SUSCRIPCION_CANCELADA");
        assertThat(dto.getMonto()).isEqualTo(19990.0);
        assertThat(dto.getFechaInicio()).isEqualTo(inicio);
        assertThat(dto.getFechaFin()).isEqualTo(fin);
        assertThat(dto.getFechaEvento()).isEqualTo(ahora);
    }

    @Test
    void constructorCompleto_establece_fechaEvento_automaticamente() {
        LocalDateTime antes = LocalDateTime.now();

        SuscripcionEventDTO dto = new SuscripcionEventDTO(
                1L, 1L, "a@b.com", "Nombre",
                "BASICO", "ACTIVA", "SUSCRIPCION_RENOVADA",
                5000.0, LocalDate.now(), LocalDate.now().plusMonths(1)
        );

        LocalDateTime despues = LocalDateTime.now();

        assertThat(dto.getFechaEvento()).isAfterOrEqualTo(antes);
        assertThat(dto.getFechaEvento()).isBeforeOrEqualTo(despues);
    }
}
