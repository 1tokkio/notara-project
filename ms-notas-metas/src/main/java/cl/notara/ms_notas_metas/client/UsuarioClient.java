package cl.notara.ms_notas_metas.client;

import cl.notara.ms_notas_metas.dto.UsuarioDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name="usuarioClient", url="http://localhost:8081/usuarios")
public interface UsuarioClient {

    @GetMapping("/{id}")
    UsuarioDTO getUsuario(@PathVariable Long id);
}
