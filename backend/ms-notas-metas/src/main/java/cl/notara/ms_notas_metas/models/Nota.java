package cl.notara.ms_notas_metas.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Entidad que representa una nota creada por un usuario dentro del sistema.
 *
 * <p>
 * Las notas permiten almacenar información, recordatorios, ideas o
 * cualquier contenido textual asociado a un usuario específico.
 * </p>
 *
 * <p>
 * Esta entidad se encuentra mapeada a la tabla {@code notas} de la
 * base de datos y forma parte del microservicio de gestión de notas
 * y metas.
 * </p>
 *
 * @author Notara
 * @version 1.0
 */
@Entity
@Table(name = "notas")
public class Nota {

    /**
     * Identificador único de la nota.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Título de la nota.
     *
     * <p>
     * Es un campo obligatorio que identifica de manera breve
     * el contenido de la nota.
     * </p>
     */
    @NotBlank(message = "El título es obligatorio")
    private String titulo;

    /**
     * Contenido descriptivo de la nota.
     *
     * <p>
     * Puede almacenar hasta 500 caracteres.
     * </p>
     */
    @Column(length = 500)
    private String contenido;

    /**
     * Identificador del usuario propietario de la nota.
     *
     * <p>
     * Este campo es obligatorio y permite asociar la nota
     * a un usuario específico dentro del sistema.
     * </p>
     */
    @NotNull(message = "El id del Usuario es obligatorio")
    @Column(nullable = false)
    private Long idUsuario;

    /**
     * Estado actual de la nota.
     *
     * <p>
     * El valor se almacena como texto utilizando el nombre
     * de la constante definida en la enumeración
     * {@link EstadoNota}.
     * </p>
     */
    @Enumerated(EnumType.STRING)
    private EstadoNota estado;

    /**
     * Constructor vacío requerido por JPA.
     */
    public Nota() {}

    /**
     * Constructor con todos los atributos principales.
     *
     * @param id identificador de la nota
     * @param titulo título de la nota
     * @param contenido contenido de la nota
     * @param idUsuario identificador del usuario propietario
     * @param estado estado actual de la nota
     */
    public Nota(
            Long id,
            String titulo,
            String contenido,
            Long idUsuario,
            EstadoNota estado
    ) {
        this.id = id;
        this.titulo = titulo;
        this.contenido = contenido;
        this.idUsuario = idUsuario;
        this.estado = estado;
    }

    /**
     * Obtiene el identificador de la nota.
     *
     * @return identificador único
     */
    public Long getId() {
        return id;
    }

    /**
     * Asigna el identificador de la nota.
     *
     * @param id identificador a asignar
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Obtiene el título de la nota.
     *
     * @return título de la nota
     */
    public String getTitulo() {
        return titulo;
    }

    /**
     * Asigna el título de la nota.
     *
     * @param titulo nuevo título
     */
    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    /**
     * Obtiene el contenido de la nota.
     *
     * @return contenido de la nota
     */
    public String getContenido() {
        return contenido;
    }

    /**
     * Asigna el contenido de la nota.
     *
     * @param contenido contenido a almacenar
     */
    public void setContenido(String contenido) {
        this.contenido = contenido;
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
     * Obtiene el estado actual de la nota.
     *
     * @return estado de la nota
     */
    public EstadoNota getEstado() {
        return estado;
    }

    /**
     * Asigna el estado actual de la nota.
     *
     * @param estado nuevo estado de la nota
     */
    public void setEstado(EstadoNota estado) {
        this.estado = estado;
    }
}

