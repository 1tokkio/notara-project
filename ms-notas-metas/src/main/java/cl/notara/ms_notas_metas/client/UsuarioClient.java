package cl.notara.ms_notas_metas.client;

import cl.notara.ms_notas_metas.dto.UsuarioDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
        name = "usuarioClient",
        url = "http://ms-usuarios:8081"
)
public interface UsuarioClient {

    @GetMapping("/usuarios/{id}")
    UsuarioDTO getUsuario(@PathVariable Long id);
}
