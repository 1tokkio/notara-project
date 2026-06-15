package com.notara.usuarios.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

/**
 * Entidad que representa a un usuario dentro del sistema.
 *
 * <p>
 * Esta clase se mapea a la tabla {@code usuarios} de la base de datos
 * mediante JPA. Contiene la información básica necesaria para la
 * autenticación e identificación de los usuarios registrados.
 * </p>
 *
 * <p>
 * Además, incorpora validaciones mediante Bean Validation para
 * garantizar la integridad de los datos antes de ser persistidos.
 * </p>
 *
 * @author Notara
 * @version 1.0
 */
@Entity
@Table(name = "usuarios")
public class Usuario {

    /**
     * Identificador único del usuario.
     *
     * <p>
     * Su valor es generado automáticamente por la base de datos
     * utilizando la estrategia IDENTITY.
     * </p>
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Nombre del usuario.
     *
     * <p>
     * Es un campo obligatorio y no puede estar vacío.
     * </p>
     */
    @NotBlank(message = "El nombre es obligatorio")
    @Column(nullable = false)
    private String nombre;

    /**
     * Correo electrónico del usuario.
     *
     * <p>
     * Debe tener un formato válido y ser único dentro del sistema.
     * </p>
     */
    @Email(message = "Debe ser un email válido")
    @NotBlank(message = "El email es obligatorio")
    @Column(nullable = false, unique = true)
    private String email;

    /**
     * Contraseña del usuario.
     *
     * <p>
     * Es un campo obligatorio. Antes de ser almacenada debe ser
     * cifrada mediante BCrypt para garantizar la seguridad de
     * las credenciales.
     * </p>
     */
    @NotBlank(message = "La contraseña es obligatoria")
    @Column(nullable = false)
    private String password;

    /**
     * Constructor vacío requerido por JPA.
     */
    public Usuario() {}

    /**
     * Constructor con todos los atributos.
     *
     * @param id identificador del usuario
     * @param nombre nombre del usuario
     * @param email correo electrónico del usuario
     * @param password contraseña del usuario
     */
    public Usuario(Long id, String nombre, String email, String password) {
        this.id = id;
        this.nombre = nombre;
        this.email = email;
        this.password = password;
    }

    /**
     * Obtiene el identificador del usuario.
     *
     * @return identificador único
     */
    public Long getId() {
        return id;
    }

    /**
     * Asigna el identificador del usuario.
     *
     * @param id identificador a asignar
     */
    public void setId(int id) {
        this.id = (long) id;
    }

    /**
     * Obtiene el nombre del usuario.
     *
     * @return nombre del usuario
     */
    public String getNombre() {
        return nombre;
    }

    /**
     * Asigna el nombre del usuario.
     *
     * @param nombre nombre a asignar
     */
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    /**
     * Obtiene el correo electrónico del usuario.
     *
     * @return correo electrónico
     */
    public String getEmail() {
        return email;
    }

    /**
     * Asigna el correo electrónico del usuario.
     *
     * @param email correo electrónico a asignar
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Obtiene la contraseña del usuario.
     *
     * @return contraseña almacenada
     */
    public String getPassword() {
        return password;
    }

    /**
     * Asigna la contraseña del usuario.
     *
     * @param password contraseña a asignar
     */
    public void setPassword(String password) {
        this.password = password;
    }
}