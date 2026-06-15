package cl.notara.ms_notas_metas.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Manejador global de excepciones para el microservicio de notas y metas.
 *
 * <p>
 * Esta clase centraliza el tratamiento de excepciones generadas
 * durante la ejecución de las solicitudes HTTP, permitiendo
 * devolver respuestas uniformes y descriptivas a los clientes.
 * </p>
 *
 * <p>
 * Gracias a la anotación {@code @ControllerAdvice}, Spring intercepta
 * automáticamente las excepciones lanzadas por los controladores
 * y ejecuta el método correspondiente definido en esta clase.
 * </p>
 *
 * @author Notara
 * @version 1.0
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Maneja excepciones relacionadas con recursos inexistentes.
     *
     * <p>
     * Se utiliza cuando un recurso solicitado, como una nota o meta,
     * no puede ser encontrado en la base de datos.
     * </p>
     *
     * @param ex excepción de recurso no encontrado
     * @return respuesta HTTP 404 con el mensaje de error
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<?> handleNotFound(
            ResourceNotFoundException ex
    ) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", ex.getMessage()));
    }

    /**
     * Maneja excepciones generales de tipo RuntimeException.
     *
     * <p>
     * Se utiliza para errores de lógica de negocio o validaciones
     * que no correspondan a excepciones específicas.
     * </p>
     *
     * @param ex excepción capturada
     * @return respuesta HTTP 400 con el mensaje de error
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<?> handleRuntimeException(
            RuntimeException ex
    ) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", ex.getMessage()));
    }

    /**
     * Maneja errores de validación generados por Bean Validation.
     *
     * <p>
     * Cuando un objeto anotado con {@code @Valid} no cumple las
     * restricciones definidas en la entidad o DTO, Spring genera
     * una excepción {@code MethodArgumentNotValidException}.
     * </p>
     *
     * <p>
     * Este método recopila todos los errores de validación y los
     * devuelve en formato clave-valor, donde la clave corresponde
     * al nombre del campo y el valor al mensaje de error.
     * </p>
     *
     * @param ex excepción de validación
     * @return respuesta HTTP 400 con el detalle de los errores
     */
    @ExceptionHandler(
            org.springframework.web.bind.MethodArgumentNotValidException.class
    )
    public ResponseEntity<?> handleValidationException(
            org.springframework.web.bind.MethodArgumentNotValidException ex
    ) {

        Map<String, String> errores = new HashMap<>();

        ex.getBindingResult().getFieldErrors().forEach(error ->
                errores.put(
                        error.getField(),
                        error.getDefaultMessage()
                )
        );

        return ResponseEntity.badRequest().body(errores);
    }
}
