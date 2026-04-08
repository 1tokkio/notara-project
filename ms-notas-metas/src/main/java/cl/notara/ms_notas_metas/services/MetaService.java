package cl.notara.ms_notas_metas.services;
import cl.notara.ms_notas_metas.models.Meta;
import cl.notara.ms_notas_metas.repositories.MetaRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

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

    public Optional<Meta> obtener(Long id) {
        return metaRepository.findById(id);
    }

    public void eliminar(Long id) {
        metaRepository.deleteById(id);
    }
}
