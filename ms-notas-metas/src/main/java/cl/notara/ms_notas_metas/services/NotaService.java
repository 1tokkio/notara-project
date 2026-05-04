package cl.notara.ms_notas_metas.services;

import cl.notara.ms_notas_metas.client.UsuarioClient;
import cl.notara.ms_notas_metas.dto.UsuarioDTO;
import cl.notara.ms_notas_metas.exceptions.ResourceNotFoundException;
import cl.notara.ms_notas_metas.models.EstadoNota;
import cl.notara.ms_notas_metas.models.Nota;
import cl.notara.ms_notas_metas.repositories.NotaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotaService {

    private final NotaRepository notaRepository;
    private final UsuarioClient usuarioCliente;

    public NotaService(NotaRepository notaRepository, UsuarioClient usuarioCliente) {
        this.notaRepository = notaRepository;
        this.usuarioCliente = usuarioCliente;
    }

    public List<Nota> listar() {
        return notaRepository.findAll();
    }

    public Nota guardar(Nota nota) {

        nota.setEstado(EstadoNota.PENDIENTE);

        Nota notaGuardada =
                notaRepository.save(nota);

        System.out.println(
                " Nota "
                        + notaGuardada.getId()
                        + " creada en PENDIENTE"
        );

        try {

            System.out.println(
                    " Validando usuario "
                            + nota.getIdUsuario()
                            + " en ms-usuarios"
            );

            UsuarioDTO usuario =
                    usuarioCliente.getUsuario(
                            nota.getIdUsuario()
                    );

            if (usuario != null) {

                notaGuardada.setEstado(
                        EstadoNota.CONFIRMADA
                );

                System.out.println(
                        " Nota "
                                + notaGuardada.getId()
                                + " CONFIRMADA"
                );

                return notaRepository.save(
                        notaGuardada
                );
            }

            System.out.println(
                    " Usuario no existe"
            );

            notaRepository.deleteById(
                    notaGuardada.getId()
            );

            throw new RuntimeException(
                    "Usuario no válido"
            );

        } catch (Exception e) {

            System.out.println(
                    " Error en solicitud: "
                            + e.getMessage()
            );

            notaRepository.deleteById(
                    notaGuardada.getId()
            );

            throw new RuntimeException(
                    "Solicitud cancelada: error usuario invalido"
            );
        }
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
