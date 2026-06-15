package cl.notara.ms_notas_metas.repositories;

import cl.notara.ms_notas_metas.models.Nota;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repositorio encargado de la persistencia y consulta de entidades
 * {@link Nota}.
 *
 * <p>
 * Extiende {@link JpaRepository}, proporcionando de manera automática
 * las operaciones CRUD básicas para la gestión de notas almacenadas
 * en la base de datos.
 * </p>
 *
 * <p>
 * Además, define consultas personalizadas utilizando las convenciones
 * de nombres de Spring Data JPA, permitiendo recuperar notas asociadas
 * a un usuario específico.
 * </p>
 *
 * @author Notara
 * @version 1.0
 */
public interface NotaRepository extends JpaRepository<Nota, Long> {

    /**
     * Obtiene todas las notas asociadas a un usuario.
     *
     * <p>
     * Spring Data JPA genera automáticamente la consulta utilizando
     * el campo {@code idUsuario} de la entidad {@link Nota}.
     * </p>
     *
     * @param idUsuario identificador del usuario propietario
     * @return lista de notas pertenecientes al usuario
     */
    List<Nota> findByIdUsuario(Long idUsuario);
}
