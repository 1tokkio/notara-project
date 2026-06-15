package cl.notara.ms_pagos_subscripciones.exceptions;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ResourceNotFoundExceptionTest {

    @Test
    void constructor_estableceMensajeCorrectamente() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Recurso no encontrado");

        assertThat(ex.getMessage()).isEqualTo("Recurso no encontrado");
    }

    @Test
    void esInstanciaDeRuntimeException() {
        ResourceNotFoundException ex = new ResourceNotFoundException("test");

        assertThat(ex).isInstanceOf(RuntimeException.class);
    }

    @Test
    void constructor_conMensajeConId_estableceMensaje() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Suscripción no encontrada con id: 42");

        assertThat(ex.getMessage()).isEqualTo("Suscripción no encontrada con id: 42");
        assertThat(ex.getMessage()).contains("42");
    }
}
