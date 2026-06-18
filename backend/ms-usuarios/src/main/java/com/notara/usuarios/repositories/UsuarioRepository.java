package com.notara.usuarios.repositories;

import com.notara.usuarios.models.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/**
 * Repositorio encargado de la persistencia y consulta de datos
 * relacionados con la entidad {@link Usuario}.
 *
 * <p>
 * Extiende {@link JpaRepository}, proporcionando automáticamente
 * operaciones CRUD para la gestión de usuarios, incluyendo:
 * </p>
 *
 * <ul>
 *     <li>Creación de usuarios.</li>
 *     <li>Consulta de usuarios por identificador.</li>
 *     <li>Actualización de información.</li>
 *     <li>Eliminación de registros.</li>
 * </ul>
 *
 * <p>
 * Además, define métodos personalizados para la búsqueda y validación
 * de usuarios mediante su correo electrónico.
 * </p>
 *
 * @author Notara
 * @version 1.0
 */
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    /**
     * Busca un usuario a partir de su dirección de correo electrónico.
     *
     * <p>
     * Este método es utilizado principalmente durante los procesos
     * de autenticación y consulta de información de usuarios.
     * </p>
     *
     * @param email correo electrónico del usuario
     * @return un {@link Optional} que contiene el usuario encontrado,
     *         o vacío si no existe un usuario con dicho correo
     */
    Optional<Usuario> findByEmail(String email);

    /**
     * Verifica si existe un usuario registrado con el correo electrónico indicado.
     *
     * <p>
     * Este método suele emplearse durante el proceso de registro para
     * evitar la creación de cuentas duplicadas.
     * </p>
     *
     * @param email correo electrónico a verificar
     * @return {@code true} si el correo ya se encuentra registrado,
     *         {@code false} en caso contrario
     */
    boolean existsByEmail(String email);

    /**
     * Ejecuta el stored procedure buscar_usuario_por_email.
     *
     * <p>
     * Utilizado para busquedas de usuario por email a traves de un
     * procedimiento almacenado en PostgreSQL, demostrando el uso
     * de JPA + Stored Procedures en la arquitectura de microservicios.
     * </p>
     *
     * @param email correo electronico a buscar
     * @return usuario encontrado, o null si no existe
     */
    @Procedure(procedureName = "buscar_usuario_por_email")
    Optional<Usuario> buscarPorEmailSP(@Param("p_email") String email);

}
