package cl.notara.ms_notas_metas.exceptions;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@ControllerAdvice

public class GlobalExceptionHandler {
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<?> manejarError(RuntimeException e) {
        return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
    }
}
