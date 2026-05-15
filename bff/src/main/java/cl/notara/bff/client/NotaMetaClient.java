package cl.notara.bff.client;

import cl.notara.bff.model.Meta;
import cl.notara.bff.model.Nota;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "notas-metas",
        url = "http://api-gateway001:8086/api")
public interface NotaMetaClient {

    @PostMapping("/notas")
    Nota crearNota(@RequestBody Nota nota);

    @GetMapping("/notas/usuario/{idUsuario}")
    List<Nota> obtenerNotas(
            @PathVariable Long idUsuario
    );

    @PostMapping("/metas")
    Meta crearMeta(@RequestBody Meta meta);

    @GetMapping("/metas/usuario/{idUsuario}")
    List<Meta> obtenerMetas(
            @PathVariable Long idUsuario
    );
}
