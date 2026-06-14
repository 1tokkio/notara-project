package cl.notara.ms_pagos_subscripciones.exceptions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    @DisplayName("handleNotFound() → 404 con mensaje correcto")
    void handleNotFound_retorna404() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Suscripción no encontrada con id: 1");

        ResponseEntity<Map<String, Object>> response = handler.handleNotFound(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).containsKey("mensaje");
        assertThat(response.getBody()).containsKey("timestamp");
        assertThat(response.getBody().get("status")).isEqualTo(404);
        assertThat(response.getBody().get("mensaje")).isEqualTo("Suscripción no encontrada con id: 1");
    }

    @Test
    @DisplayName("handleIllegalState() → 409 con mensaje correcto")
    void handleIllegalState_retorna409() {
        IllegalStateException ex = new IllegalStateException("El usuario ya tiene una suscripción activa");

        ResponseEntity<Map<String, Object>> response = handler.handleIllegalState(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().get("status")).isEqualTo(409);
        assertThat(response.getBody().get("mensaje")).isEqualTo("El usuario ya tiene una suscripción activa");
    }

    @Test
    @DisplayName("handleRuntime() → 400 con mensaje correcto")
    void handleRuntime_retorna400() {
        RuntimeException ex = new RuntimeException("Error inesperado");

        ResponseEntity<Map<String, Object>> response = handler.handleRuntime(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().get("status")).isEqualTo(400);
    }

    @Test
    @DisplayName("handleValidation() → 400 con mapa de errores de campos")
    void handleValidation_retorna400ConErrores() throws Exception {
        BeanPropertyBindingResult bindingResult =
                new BeanPropertyBindingResult(new Object(), "suscripcion");
        bindingResult.addError(new FieldError("suscripcion", "emailUsuario", "El email es obligatorio"));
        bindingResult.addError(new FieldError("suscripcion", "monto", "El monto debe ser mayor a cero"));

        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(null, bindingResult);

        ResponseEntity<Map<String, Object>> response = handler.handleValidation(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).containsKey("errores");

        @SuppressWarnings("unchecked")
        Map<String, String> errores = (Map<String, String>) response.getBody().get("errores");
        assertThat(errores).containsKey("emailUsuario");
        assertThat(errores).containsKey("monto");
    }
}
