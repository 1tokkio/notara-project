package cl.notara.ms_pagos_subscripciones.exceptions;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleNotFound_retorna404ConMensaje() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Recurso no encontrado");

        ResponseEntity<Map<String, Object>> response = handler.handleNotFound(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).containsEntry("status", 404);
        assertThat(response.getBody()).containsEntry("mensaje", "Recurso no encontrado");
        assertThat(response.getBody()).containsKey("timestamp");
        assertThat(response.getBody()).containsKey("error");
    }

    @Test
    void handleIllegalState_retorna409ConMensaje() {
        IllegalStateException ex = new IllegalStateException("Conflicto de estado");

        ResponseEntity<Map<String, Object>> response = handler.handleIllegalState(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).containsEntry("status", 409);
        assertThat(response.getBody()).containsEntry("mensaje", "Conflicto de estado");
    }

    @Test
    void handleRuntime_retorna400ConMensaje() {
        RuntimeException ex = new RuntimeException("Error inesperado");

        ResponseEntity<Map<String, Object>> response = handler.handleRuntime(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).containsEntry("status", 400);
        assertThat(response.getBody()).containsEntry("mensaje", "Error inesperado");
    }

    @Test
    void handleNotFound_conMensajeNull_retorna404() {
        ResourceNotFoundException ex = new ResourceNotFoundException(null);

        ResponseEntity<Map<String, Object>> response = handler.handleNotFound(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).containsEntry("status", 404);
    }

    @Test
    void handleIllegalState_conMensajeDetallado_retorna409() {
        IllegalStateException ex = new IllegalStateException("El usuario ya tiene una suscripción activa");

        ResponseEntity<Map<String, Object>> response = handler.handleIllegalState(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).containsEntry("mensaje", "El usuario ya tiene una suscripción activa");
        assertThat(response.getBody()).containsEntry("error", "Conflict");
    }
}
