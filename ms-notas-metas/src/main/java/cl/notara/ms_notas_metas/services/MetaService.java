package cl.notara.ms_notas_metas.services;

import cl.notara.ms_notas_metas.client.UsuarioClient;
import cl.notara.ms_notas_metas.dto.UsuarioDTO;
import cl.notara.ms_notas_metas.exceptions.ResourceNotFoundException;
import cl.notara.ms_notas_metas.models.Meta;
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
        try {
            UsuarioDTO user = usuarioCliente.getUsuario(meta.getIdUsuario());

            if (user == null) {
                throw new ResourceNotFoundException("El usuario con ID " + meta.getIdUsuario() + " no fue encontrado en el sistema externo.");
            }

            System.out.println("Creando meta para el usuario: " + user.getNombre());

        } catch (Exception e) {
            throw new ResourceNotFoundException("Error al validar usuario: El ID " + meta.getIdUsuario() + " no existe o el servicio no está disponible.");
        }
        return metaRepository.save(meta);
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
