package cl.notara.ms_notas_metas.services;

import cl.notara.ms_notas_metas.models.Nota;
import cl.notara.ms_notas_metas.repositories.NotaRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class NotaService {

    private final NotaRepository notaRepository;

    public NotaService(NotaRepository notaRepository) {
        this.notaRepository = notaRepository;
    }

    public List<Nota> listar() {
        return notaRepository.findAll();
    }

    public Nota guardar(Nota nota) {
        return notaRepository.save(nota);
    }

    public Optional<Nota> obtener(Long id) {
        return notaRepository.findById(id);
    }

    public void eliminar(Long id) {
        notaRepository.deleteById(id);
    }
}
