package cl.notara.ms_notas_metas.services;

import cl.notara.ms_notas_metas.exceptions.ResourceNotFoundException;
import cl.notara.ms_notas_metas.models.Meta;
import cl.notara.ms_notas_metas.repositories.MetaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MetaService {

    private final MetaRepository metaRepository;

    public MetaService(MetaRepository metaRepository) {
        this.metaRepository = metaRepository;
    }

    public List<Meta> listar() {
        return metaRepository.findAll();
    }

    public Meta guardar(Meta meta) {
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
