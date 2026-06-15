package cl.notara.ms_notas_metas.client;

import cl.notara.ms_notas_metas.dto.UsuarioDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Cliente Feign utilizado para la comunicación con el microservicio
 * de usuarios.
 *
 * <p>
 * Esta interfaz permite consumir endpoints expuestos por el servicio
 * de usuarios sin necesidad de implementar manualmente llamadas HTTP.
 * Spring Cloud OpenFeign genera automáticamente la implementación
 * en tiempo de ejecución.
 * </p>
 *
 * <p>
 * El cliente se conecta al microservicio de usuarios mediante la URL
 * configurada y permite recuperar información de usuarios requerida
 * por el microservicio de notas y metas.
 * </p>
 *
 * @author Notara
 * @version 1.0
 */
@FeignClient(
        name = "usuarioClient",
        url = "http://ms-usuarios:8081"
)
public interface UsuarioClient {

    /**
     * Obtiene la información de un usuario a partir de su identificador.
     *
     * <p>
     * Realiza una solicitud HTTP GET al endpoint del microservicio
     * de usuarios y devuelve los datos recibidos en forma de
     * {@link UsuarioDTO}.
     * </p>
     *
     * @param id identificador del usuario
     * @return información del usuario solicitada
     */
    @GetMapping("/usuarios/{id}")
    UsuarioDTO getUsuario(@PathVariable Long id);
}
