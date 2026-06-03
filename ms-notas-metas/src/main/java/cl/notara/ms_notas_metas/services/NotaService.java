package cl.notara.ms_notas_metas.services;

import cl.notara.ms_notas_metas.client.UsuarioClient;
import cl.notara.ms_notas_metas.dto.UsuarioDTO;
import cl.notara.ms_notas_metas.exceptions.ResourceNotFoundException;
import cl.notara.ms_notas_metas.models.EstadoNota;
import cl.notara.ms_notas_metas.models.Nota;
import cl.notara.ms_notas_metas.repositories.NotaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Servicio encargado de la lógica de negocio relacionada con las notas.
 *
 * <p>
 * Gestiona las operaciones de creación, consulta, actualización y
 * eliminación de notas dentro del sistema. Además, valida la existencia
 * del usuario propietario mediante comunicación con el microservicio
 * de usuarios.
 * </p>
 *
 * <p>
 * Implementa una validación distribuida utilizando OpenFeign para
 * asegurar que las notas solamente puedan asociarse a usuarios válidos.
 * </p>
 *
 * @author Notara
 * @version 1.0
 */
@Service
public class NotaService {

    /**
     * Repositorio encargado de la persistencia de notas.
     */
    private final NotaRepository notaRepository;

    /**
     * Cliente Feign utilizado para comunicarse con el microservicio
     * de usuarios.
     */
    private final UsuarioClient usuarioCliente;

    /**
     * Constructor que inyecta las dependencias necesarias.
     *
     * @param notaRepository repositorio de notas
     * @param usuarioCliente cliente del microservicio de usuarios
     */
    public NotaService(
            NotaRepository notaRepository,
            UsuarioClient usuarioCliente
    ) {
        this.notaRepository = notaRepository;
        this.usuarioCliente = usuarioCliente;
    }

    /**
     * Obtiene todas las notas registradas.
     *
     * @return lista completa de notas
     */
    public List<Nota> listar() {
        return notaRepository.findAll();
    }

    /**
     * Crea una nueva nota y valida la existencia del usuario asociado.
     *
     * <p>
     * La nota es almacenada inicialmente con estado
     * {@link EstadoNota#PENDIENTE}. Posteriormente se consulta el
     * microservicio de usuarios para verificar la existencia del
     * propietario.
     * </p>
     *
     * <p>
     * Si el usuario existe, la nota cambia su estado a
     * {@link EstadoNota#CONFIRMADA}. En caso contrario, la nota es
     * eliminada y se genera una excepción.
     * </p>
     *
     * @param nota nota a registrar
     * @return nota validada y almacenada
     * @throws IllegalArgumentException si el usuario no es válido
     * @throws RuntimeException si ocurre un error durante la validación
     */
    public Nota guardar(Nota nota) {

        nota.setEstado(EstadoNota.PENDIENTE);

        Nota notaGuardada = notaRepository.save(nota);

        System.out.println(
                "Nota " + notaGuardada.getId()
                        + " creada en PENDIENTE"
        );

        try {

            System.out.println(
                    "Validando usuario "
                            + nota.getIdUsuario()
                            + " en ms-usuarios"
            );

            UsuarioDTO usuario =
                    usuarioCliente.getUsuario(
                            nota.getIdUsuario()
                    );

            if (usuario == null) {

                System.out.println(
                        "Usuario no existe"
                );

                notaRepository.deleteById(
                        notaGuardada.getId()
                );

                throw new IllegalArgumentException(
                        "Usuario no válido"
                );
            }

            notaGuardada.setEstado(
                    EstadoNota.CONFIRMADA
            );

            System.out.println(
                    "Nota "
                            + notaGuardada.getId()
                            + " CONFIRMADA"
            );

            return notaRepository.save(
                    notaGuardada
            );

        } catch (IllegalArgumentException e) {

            throw e;

        } catch (Exception e) {

            System.out.println(
                    "Error en solicitud: "
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

    /**
     * Obtiene una nota a partir de su identificador.
     *
     * @param id identificador de la nota
     * @return nota encontrada
     * @throws ResourceNotFoundException si la nota no existe
     */
    public Nota obtener(Long id) {
        return notaRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Nota no encontrada con id: " + id
                        )
                );
    }

    /**
     * Obtiene todas las notas asociadas a un usuario.
     *
     * @param idUsuario identificador del usuario
     * @return lista de notas pertenecientes al usuario
     */
    public List<Nota> obtenerPorUsuario(Long idUsuario) {
        return notaRepository.findByIdUsuario(idUsuario);
    }

    /**
     * Elimina una nota existente.
     *
     * @param id identificador de la nota
     * @throws ResourceNotFoundException si la nota no existe
     */
    public void eliminar(Long id) {

        if (!notaRepository.existsById(id)) {
            throw new ResourceNotFoundException(
                    "Nota no encontrada con id: " + id
            );
        }

        notaRepository.deleteById(id);
    }

    /**
     * Actualiza la información de una nota existente.
     *
     * @param id identificador de la nota
     * @param notaActualizada nuevos datos de la nota
     * @return nota actualizada
     * @throws ResourceNotFoundException si la nota no existe
     */
    public Nota actualizar(
            Long id,
            Nota notaActualizada
    ) {

        Nota nota = notaRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Nota no encontrada con id: " + id
                        )
                );

        nota.setTitulo(notaActualizada.getTitulo());
        nota.setContenido(notaActualizada.getContenido());
        nota.setIdUsuario(notaActualizada.getIdUsuario());

        return notaRepository.save(nota);
    }
}
