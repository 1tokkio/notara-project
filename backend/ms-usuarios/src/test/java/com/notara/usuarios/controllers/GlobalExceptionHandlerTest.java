package com.notara.usuarios.controllers;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    @DisplayName("handleRuntimeException - retorna 400 con mensaje de error")
    void handleRuntimeException_retorna400() {
        RuntimeException ex = new RuntimeException("algo salió mal");

        ResponseEntity<?> response = handler.handleRuntimeException(ex);

        assertEquals(400, response.getStatusCode().value());
        @SuppressWarnings("unchecked")
        Map<String, String> body = (Map<String, String>) response.getBody();
        assertNotNull(body);
        assertEquals("algo salió mal", body.get("error"));
    }

    @Test
    @DisplayName("handleRuntimeException - retorna 400 con mensaje específico de contraseña")
    void handleRuntimeException_mensajeContrasena() {
        RuntimeException ex = new RuntimeException("Contraseña incorrecta");

        ResponseEntity<?> response = handler.handleRuntimeException(ex);

        assertEquals(400, response.getStatusCode().value());
        @SuppressWarnings("unchecked")
        Map<String, String> body = (Map<String, String>) response.getBody();
        assertNotNull(body);
        assertEquals("Contraseña incorrecta", body.get("error"));
    }

    @Test
    @DisplayName("handleRuntimeException - retorna 400 con mensaje de email duplicado")
    void handleRuntimeException_emailDuplicado() {
        RuntimeException ex = new RuntimeException("El email ya está registrado");

        ResponseEntity<?> response = handler.handleRuntimeException(ex);

        assertEquals(400, response.getStatusCode().value());
        @SuppressWarnings("unchecked")
        Map<String, String> body = (Map<String, String>) response.getBody();
        assertEquals("El email ya está registrado", body.get("error"));
    }
}
