package cl.notara.ms_notas_metas.exceptions;

/**
 * Excepción personalizada utilizada para indicar que un recurso
 * solicitado no fue encontrado en el sistema.
 *
 * <p>
 * Esta excepción se emplea principalmente cuando una entidad,
 * como una nota o una meta, no existe en la base de datos y
 * se intenta acceder a ella mediante su identificador.
 * </p>
 *
 * <p>
 * Es gestionada por {@link GlobalExceptionHandler}, que transforma
 * la excepción en una respuesta HTTP 404 (Not Found) para el cliente.
 * </p>
 *
 * @author Notara
 * @version 1.0
 */
public class ResourceNotFoundException extends RuntimeException {

    /**
     * Crea una nueva excepción de recurso no encontrado.
     *
     * @param message mensaje descriptivo del error
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
