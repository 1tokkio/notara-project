package cl.notara.bff.client;

import cl.notara.bff.model.Usuario;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "usuarios",
        url = "http://localhost:8086/api")
public interface UsuarioClient {

    @GetMapping("/usuarios/{id}")
    Usuario obtenerUsuario(
            @PathVariable Long id
    );
}
