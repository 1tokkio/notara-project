package com.notara.usuarios.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Manejador global de excepciones de la aplicación.
 *
 * <p>
 * Esta clase centraliza el tratamiento de excepciones lanzadas por los
 * controladores REST, permitiendo devolver respuestas HTTP consistentes
 * y amigables para el cliente.
 * </p>
 *
 * <p>
 * Mediante la anotación {@code @ControllerAdvice}, Spring intercepta
 * automáticamente las excepciones definidas en esta clase y ejecuta
 * el método correspondiente para generar la respuesta adecuada.
 * </p>
 *
 * @author Notara
 * @version 1.0
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Captura cualquier excepción de tipo {@link RuntimeException}
     * generada durante el procesamiento de una solicitud.
     *
     * <p>
     * Retorna una respuesta HTTP 400 (Bad Request) con un mensaje
     * descriptivo del error en formato JSON.
     * </p>
     *
     * <p>
     * Ejemplo de respuesta:
     * </p>
     *
     * <pre>
     * {
     *   "error": "El correo ya se encuentra registrado"
     * }
     * </pre>
     *
     * @param ex excepción capturada
     * @return respuesta HTTP con el mensaje de error
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<?> handleRuntimeException(RuntimeException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", ex.getMessage()));
    }
}
