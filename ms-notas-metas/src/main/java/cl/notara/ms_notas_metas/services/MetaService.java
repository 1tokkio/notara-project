package cl.notara.ms_notas_metas.services;

import cl.notara.ms_notas_metas.client.UsuarioClient;
import cl.notara.ms_notas_metas.dto.UsuarioDTO;
import cl.notara.ms_notas_metas.exceptions.ResourceNotFoundException;
import cl.notara.ms_notas_metas.models.EstadoMeta;
import cl.notara.ms_notas_metas.models.Meta;
import cl.notara.ms_notas_metas.repositories.MetaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Servicio encargado de la lógica de negocio relacionada con las metas.
 *
 * <p>
 * Gestiona las operaciones de creación, consulta, actualización y
 * eliminación de metas, además de validar la existencia del usuario
 * propietario mediante comunicación con el microservicio de usuarios.
 * </p>
 *
 * <p>
 * Implementa una estrategia de validación distribuida utilizando
 * OpenFeign, garantizando que las metas solamente puedan asociarse
 * a usuarios válidos registrados en el sistema.
 * </p>
 *
 * @author Notara
 * @version 1.0
 */
@Service
public class MetaService {

    /**
     * Repositorio encargado de la persistencia de metas.
     */
    private final MetaRepository metaRepository;

    /**
     * Cliente Feign utilizado para validar usuarios en el microservicio
     * de usuarios.
     */
    private final UsuarioClient usuarioCliente;

    /**
     * Constructor que inyecta las dependencias necesarias.
     *
     * @param metaRepository repositorio de metas
     * @param usuarioCliente cliente del microservicio de usuarios
     */
    public MetaService(
            MetaRepository metaRepository,
            UsuarioClient usuarioCliente
    ) {
        this.metaRepository = metaRepository;
        this.usuarioCliente = usuarioCliente;
    }

    /**
     * Obtiene todas las metas registradas.
     *
     * @return lista de metas
     */
    public List<Meta> listar() {
        return metaRepository.findAll();
    }

    /**
     * Crea una nueva meta y valida la existencia del usuario asociado.
     *
     * <p>
     * Inicialmente la meta es almacenada con estado
     * {@link EstadoMeta#PENDIENTE}. Posteriormente se consulta
     * el microservicio de usuarios para verificar la existencia
     * del propietario.
     * </p>
     *
     * <p>
     * Si el usuario existe, la meta cambia su estado a
     * {@link EstadoMeta#CONFIRMADA}. En caso contrario,
     * la meta es eliminada y se genera una excepción.
     * </p>
     *
     * @param meta meta a registrar
     * @return meta validada y almacenada
     * @throws IllegalArgumentException si el usuario no es válido
     * @throws RuntimeException si ocurre un error durante la validación
     */
    public Meta guardar(Meta meta) {

        meta.setEstado(EstadoMeta.PENDIENTE);

        Meta metaGuardada = metaRepository.save(meta);

        System.out.println(
                "Meta " + metaGuardada.getId() + " creada en PENDIENTE"
        );

        try {

            System.out.println(
                    "Validando usuario "
                            + meta.getIdUsuario()
                            + " en ms-usuarios"
            );

            UsuarioDTO usuario =
                    usuarioCliente.getUsuario(
                            meta.getIdUsuario()
                    );

            if (usuario == null) {

                System.out.println("Usuario no existe");

                metaRepository.deleteById(
                        metaGuardada.getId()
                );

                throw new IllegalArgumentException(
                        "Usuario no válido"
                );
            }

            metaGuardada.setEstado(
                    EstadoMeta.CONFIRMADA
            );

            System.out.println(
                    "Meta "
                            + metaGuardada.getId()
                            + " CONFIRMADA"
            );

            return metaRepository.save(
                    metaGuardada
            );

        } catch (IllegalArgumentException e) {

            throw e;

        } catch (Exception e) {

            System.out.println(
                    "Error en solicitud: "
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

    /**
     * Obtiene una meta mediante su identificador.
     *
     * @param id identificador de la meta
     * @return meta encontrada
     * @throws ResourceNotFoundException si la meta no existe
     */
    public Meta obtener(Long id) {
        return metaRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Meta no encontrada con id: " + id
                        )
                );
    }

    /**
     * Obtiene todas las metas asociadas a un usuario.
     *
     * @param idUsuario identificador del usuario
     * @return lista de metas pertenecientes al usuario
     */
    public List<Meta> obtenerPorUsuario(Long idUsuario) {
        return metaRepository.findByIdUsuario(idUsuario);
    }

    /**
     * Elimina una meta existente.
     *
     * @param id identificador de la meta
     * @throws ResourceNotFoundException si la meta no existe
     */
    public void eliminar(Long id) {

        if (!metaRepository.existsById(id)) {
            throw new ResourceNotFoundException(
                    "Meta no encontrada con id: " + id
            );
        }

        metaRepository.deleteById(id);
    }

    /**
     * Actualiza la información de una meta existente.
     *
     * @param id identificador de la meta
     * @param metaActualizada nuevos datos de la meta
     * @return meta actualizada
     * @throws ResourceNotFoundException si la meta no existe
     */
    public Meta actualizar(
            Long id,
            Meta metaActualizada
    ) {

        Meta meta = metaRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Meta no encontrada con id: " + id
                        )
                );

        meta.setNombre(metaActualizada.getNombre());
        meta.setDescripcion(metaActualizada.getDescripcion());
        meta.setFechaLimite(metaActualizada.getFechaLimite());
        meta.setCompletada(metaActualizada.isCompletada());
        meta.setIdUsuario(metaActualizada.getIdUsuario());

        return metaRepository.save(meta);
    }
}
