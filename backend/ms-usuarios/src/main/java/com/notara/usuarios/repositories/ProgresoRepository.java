package com.notara.usuarios.repositories;

import com.notara.usuarios.models.Progreso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio encargado de la persistencia de datos relacionados
 * con el progreso de los usuarios.
 *
 * <p>
 * Extiende {@link JpaRepository}, proporcionando automáticamente
 * operaciones CRUD para la entidad {@link Progreso}, tales como:
 * </p>
 *
 * <ul>
 *     <li>Guardar registros de progreso.</li>
 *     <li>Consultar progreso por identificador.</li>
 *     <li>Actualizar información existente.</li>
 *     <li>Eliminar registros.</li>
 * </ul>
 *
 * <p>
 * Además, define métodos personalizados para realizar búsquedas
 * específicas sobre la entidad Progreso.
 * </p>
 *
 * @author Notara
 * @version 1.0
 */
@Repository
public interface ProgresoRepository extends JpaRepository<Progreso, Long> {

    /**
     * Busca el progreso asociado al correo electrónico de un usuario.
     *
     * <p>
     * Spring Data JPA genera automáticamente la consulta a partir
     * del nombre del método utilizando la convención de nombres.
     * </p>
     *
     * @param usuarioEmail correo electrónico del usuario
     * @return un {@link Optional} que contiene el progreso encontrado,
     *         o vacío si no existe un registro asociado
     */
    Optional<Progreso> findByUsuarioEmail(String usuarioEmail);
}
