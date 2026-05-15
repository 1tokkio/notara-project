package cl.notara.ms_notas_metas.services;

import cl.notara.ms_notas_metas.client.UsuarioClient;
import cl.notara.ms_notas_metas.dto.UsuarioDTO;
import cl.notara.ms_notas_metas.exceptions.ResourceNotFoundException;
import cl.notara.ms_notas_metas.models.EstadoMeta;
import cl.notara.ms_notas_metas.models.EstadoNota;
import cl.notara.ms_notas_metas.models.Meta;
import cl.notara.ms_notas_metas.models.Nota;
import cl.notara.ms_notas_metas.repositories.MetaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MetaService {

    private final MetaRepository metaRepository;
    private final UsuarioClient usuarioCliente;

    public MetaService(MetaRepository metaRepository, UsuarioClient usuarioCliente) {
        this.metaRepository = metaRepository;
        this.usuarioCliente = usuarioCliente;
    }

    public List<Meta> listar() {
        return metaRepository.findAll();
    }

    public Meta guardar(Meta meta) {

        meta.setEstado(EstadoMeta.PENDIENTE);

        Meta metaGuardada =
                metaRepository.save(meta);

        System.out.println(
                " Meta "
                        + metaGuardada.getId()
                        + " creada en PENDIENTE"
        );

        try {

            System.out.println(
                    " Validando usuario "
                            + meta.getIdUsuario()
                            + " en ms-usuarios"
            );

            UsuarioDTO usuario =
                    usuarioCliente.getUsuario(
                            meta.getIdUsuario()
                    );

            if (usuario != null) {

                metaGuardada.setEstado(
                        EstadoMeta.CONFIRMADA
                );

                System.out.println(
                        " Meta "
                                + metaGuardada.getId()
                                + " CONFIRMADA"
                );

                return metaRepository.save(
                        metaGuardada
                );
            }

            System.out.println(
                    " Usuario no existe"
            );

            metaRepository.deleteById(
                    metaGuardada.getId()
            );

            throw new RuntimeException(
                    "Usuario no válido"
            );

        } catch (Exception e) {

            System.out.println(
                    " Error en solicitud: "
                            + e.getMessage()
            );

            metaRepository.deleteById(
                    metaGuardada.getId()
            );

            throw new RuntimeException(
                    "Solicitud cancelada: error usuario invalido"
            );
        }
    }

    public Meta obtener(Long id) {
        return metaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Meta no encontrada con id: " + id));
    }

    public List<Meta> obtenerPorUsuario(Long idUsuario) {
        return metaRepository.findByIdUsuario(idUsuario);
    }

    public void eliminar(Long id) {
        if (!metaRepository.existsById(id)) {
            throw new ResourceNotFoundException("Meta no encontrada con id: " + id);
        }
        metaRepository.deleteById(id);
    }

    public Meta actualizar(Long id, Meta metaActualizada) {
        Meta meta = metaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Meta no encontrada con id: " + id));

        meta.setNombre(metaActualizada.getNombre());
        meta.setDescripcion(metaActualizada.getDescripcion());
        meta.setFechaLimite(metaActualizada.getFechaLimite());
        meta.setCompletada(metaActualizada.isCompletada());
        meta.setIdUsuario(metaActualizada.getIdUsuario());

        return metaRepository.save(meta);
    }
}
