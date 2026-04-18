package cl.notara.ms_notas_metas.services;

import cl.notara.ms_notas_metas.exceptions.ResourceNotFoundException;
import cl.notara.ms_notas_metas.models.Nota;
import cl.notara.ms_notas_metas.repositories.NotaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

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

    public Nota obtener(Long id) {
        return notaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Nota no encontrada con id: " + id));
    }

    public List<Nota> obtenerPorUsuario(Long idUsuario) {
        return notaRepository.findByIdUsuario(idUsuario);
    }

    public void eliminar(Long id) {
        if (!notaRepository.existsById(id)) {
            throw new ResourceNotFoundException("Nota no encontrada con id: " + id);
        }
        notaRepository.deleteById(id);
    }

    public Nota actualizar(Long id, Nota notaActualizada) {
        Nota nota = notaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Nota no encontrada con id: " + id));

        nota.setTitulo(notaActualizada.getTitulo());
        nota.setContenido(notaActualizada.getContenido());
        nota.setIdUsuario(notaActualizada.getIdUsuario());

        return notaRepository.save(nota);
    }
}
