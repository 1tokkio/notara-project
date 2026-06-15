package cl.notara.ms_notas_metas.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

/**
 * Entidad que representa una meta definida por un usuario dentro del sistema.
 *
 * <p>
 * Las metas permiten a los usuarios establecer objetivos personales,
 * registrar fechas límite y realizar seguimiento de su estado de
 * cumplimiento.
 * </p>
 *
 * <p>
 * Esta entidad se encuentra mapeada a la tabla {@code metas} y almacena
 * la información necesaria para la gestión de objetivos dentro de la
 * plataforma.
 * </p>
 *
 * @author Notara
 * @version 1.0
 */
@Entity
@Table(name = "metas")
public class Meta {

    /**
     * Identificador único de la meta.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Nombre o título de la meta.
     *
     * <p>
     * Este campo es obligatorio y representa el objetivo principal
     * definido por el usuario.
     * </p>
     */
    @NotBlank(message = "El nombre de la meta es obligatorio")
    private String nombre;

    /**
     * Descripción detallada de la meta.
     */
    private String descripcion;

    /**
     * Fecha límite establecida para completar la meta.
     */
    private LocalDate fechaLimite;

    /**
     * Indica si la meta ha sido completada.
     *
     * <p>
     * Su valor por defecto es {@code false}.
     * </p>
     */
    private boolean completada = false;

    /**
     * Identificador del usuario propietario de la meta.
     *
     * <p>
     * Este campo es obligatorio y permite asociar la meta
     * con un usuario específico.
     * </p>
     */
    @NotNull(message = "El idUsuario es obligatorio")
    @Column(nullable = false)
    private Long idUsuario;

    /**
     * Estado actual de la meta.
     *
     * <p>
     * Se almacena como texto utilizando el nombre de la constante
     * definida en la enumeración {@link EstadoMeta}.
     * </p>
     */
    @Enumerated(EnumType.STRING)
    private EstadoMeta estado;

    /**
     * Constructor vacío requerido por JPA.
     */
    public Meta() {}

    /**
     * Constructor con todos los atributos principales.
     *
     * @param id identificador de la meta
     * @param nombre nombre de la meta
     * @param descripcion descripción de la meta
     * @param fechaLimite fecha límite de cumplimiento
     * @param idUsuario identificador del usuario propietario
     * @param estado estado actual de la meta
     */
    public Meta(
            Long id,
            String nombre,
            String descripcion,
            LocalDate fechaLimite,
            Long idUsuario,
            EstadoMeta estado
    ) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.fechaLimite = fechaLimite;
        this.idUsuario = idUsuario;
        this.estado = estado;
    }

    /**
     * Obtiene el identificador de la meta.
     *
     * @return identificador único
     */
    public Long getId() {
        return id;
    }

    /**
     * Asigna el identificador de la meta.
     *
     * @param id identificador a asignar
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Obtiene el nombre de la meta.
     *
     * @return nombre de la meta
     */
    public String getNombre() {
        return nombre;
    }

    /**
     * Asigna el nombre de la meta.
     *
     * @param nombre nombre de la meta
     */
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    /**
     * Obtiene la descripción de la meta.
     *
     * @return descripción de la meta
     */
    public String getDescripcion() {
        return descripcion;
    }

    /**
     * Asigna la descripción de la meta.
     *
     * @param descripcion descripción de la meta
     */
    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    /**
     * Obtiene la fecha límite de cumplimiento.
     *
     * @return fecha límite
     */
    public LocalDate getFechaLimite() {
        return fechaLimite;
    }

    /**
     * Asigna la fecha límite de cumplimiento.
     *
     * @param fechaLimite fecha límite
     */
    public void setFechaLimite(LocalDate fechaLimite) {
        this.fechaLimite = fechaLimite;
    }

    /**
     * Indica si la meta se encuentra completada.
     *
     * @return true si está completada, false en caso contrario
     */
    public boolean isCompletada() {
        return completada;
    }

    /**
     * Actualiza el estado de completitud de la meta.
     *
     * @param completada estado de completitud
     */
    public void setCompletada(boolean completada) {
        this.completada = completada;
    }

    /**
     * Obtiene el identificador del usuario propietario.
     *
     * @return identificador del usuario
     */
    public Long getIdUsuario() {
        return idUsuario;
    }

    /**
     * Asigna el identificador del usuario propietario.
     *
     * @param idUsuario identificador del usuario
     */
    public void setIdUsuario(Long idUsuario) {
        this.idUsuario = idUsuario;
    }

    /**
     * Obtiene el estado actual de la meta.
     *
     * @return estado de la meta
     */
    public EstadoMeta getEstado() {
        return estado;
    }

    /**
     * Asigna el estado actual de la meta.
     *
     * @param estado nuevo estado de la meta
     */
    public void setEstado(EstadoMeta estado) {
        this.estado = estado;
    }
}
