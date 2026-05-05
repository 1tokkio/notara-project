package cl.notara.bff.service;

import cl.notara.bff.client.NotaMetaClient;
import cl.notara.bff.client.UsuarioClient;
import cl.notara.bff.dto.DashboardDTO;
import cl.notara.bff.model.Meta;
import cl.notara.bff.model.Nota;
import cl.notara.bff.model.Usuario;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BffService {

    private final UsuarioClient usuarioClient;
    private final NotaMetaClient notaMetaClient;

    public BffService(
            UsuarioClient usuarioClient,
            NotaMetaClient notaMetaClient) {

        this.usuarioClient = usuarioClient;
        this.notaMetaClient = notaMetaClient;
    }

    public DashboardDTO obtenerDashboard(Long idUsuario) {

        Usuario usuario =
                usuarioClient.obtenerUsuario(idUsuario);

        List<Nota> notas =
                notaMetaClient.obtenerNotas(idUsuario);

        List<Meta> metas =
                notaMetaClient.obtenerMetas(idUsuario);

        DashboardDTO dashboard =
                new DashboardDTO();

        dashboard.setUsuario(usuario);
        dashboard.setNotas(notas);
        dashboard.setMetas(metas);

        return dashboard;
    }
}
