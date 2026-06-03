package cl.notara.ms_notas_metas.repositories;

import cl.notara.ms_notas_metas.models.Meta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repositorio encargado de la persistencia y consulta de entidades
 * {@link Meta}.
 *
 * <p>
 * Extiende {@link JpaRepository}, proporcionando automáticamente
 * operaciones CRUD para la gestión de metas, tales como crear,
 * actualizar, eliminar y consultar registros almacenados en la
 * base de datos.
 * </p>
 *
 * <p>
 * Además, incorpora consultas personalizadas basadas en convenciones
 * de nombres de Spring Data JPA para facilitar la obtención de metas
 * asociadas a un usuario específico.
 * </p>
 *
 * @author Notara
 * @version 1.0
 */
public interface MetaRepository extends JpaRepository<Meta, Long> {

    /**
     * Obtiene todas las metas asociadas a un usuario.
     *
     * <p>
     * Spring Data JPA genera automáticamente la consulta a partir
     * del nombre del método, filtrando los registros por el campo
     * {@code idUsuario}.
     * </p>
     *
     * @param idUsuario identificador del usuario
     * @return lista de metas pertenecientes al usuario
     */
    List<Meta> findByIdUsuario(Long idUsuario);
}
