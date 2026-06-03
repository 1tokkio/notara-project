package com.notara.usuarios.services;

import com.notara.usuarios.models.Usuario;
import com.notara.usuarios.repositories.UsuarioRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Servicio encargado de la lógica de negocio relacionada con los usuarios.
 *
 * <p>
 * Actúa como intermediario entre los controladores y el repositorio de datos,
 * gestionando operaciones de consulta, registro, autenticación y eliminación
 * de usuarios.
 * </p>
 *
 * <p>
 * Además, implementa mecanismos de seguridad para el almacenamiento de
 * contraseñas mediante el algoritmo BCrypt y valida la autenticidad de las
 * credenciales durante el proceso de inicio de sesión.
 * </p>
 *
 * @author Notara
 * @version 1.0
 */
@Service
public class UsuarioService {

    /**
     * Repositorio encargado de la persistencia de usuarios.
     */
    private final UsuarioRepository usuarioRepository;

    /**
     * Codificador de contraseñas basado en BCrypt.
     */
    private final BCryptPasswordEncoder passwordEncoder;

    /**
     * Constructor que inyecta las dependencias necesarias.
     *
     * @param usuarioRepository repositorio de usuarios
     * @param passwordEncoder codificador de contraseñas BCrypt
     */
    public UsuarioService(
            UsuarioRepository usuarioRepository,
            BCryptPasswordEncoder passwordEncoder
    ) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Obtiene la lista completa de usuarios registrados.
     *
     * @return lista de usuarios
     */
    public List<Usuario> obtenerUsuarios() {
        return usuarioRepository.findAll();
    }

    /**
     * Guarda un nuevo usuario en el sistema.
     *
     * <p>
     * Antes de almacenar el registro, verifica que no exista otro
     * usuario con el mismo correo electrónico.
     * </p>
     *
     * @param usuario usuario a registrar
     * @return usuario almacenado
     * @throws RuntimeException si el correo ya se encuentra registrado
     */
    public Usuario guardarUsuario(Usuario usuario) {

        if (usuarioRepository.existsByEmail(usuario.getEmail())) {
            throw new RuntimeException("El email ya está registrado");
        }

        return usuarioRepository.save(usuario);
    }

    /**
     * Busca un usuario mediante su identificador único.
     *
     * @param id identificador del usuario
     * @return usuario encontrado encapsulado en un Optional
     */
    public Optional<Usuario> obtenerPorId(Long id) {
        return usuarioRepository.findById(id);
    }

    /**
     * Elimina un usuario de la base de datos.
     *
     * @param id identificador del usuario a eliminar
     */
    public void eliminarUsuario(Long id) {
        usuarioRepository.deleteById(id);
    }

    /**
     * Registra un nuevo usuario aplicando cifrado a la contraseña.
     *
     * <p>
     * La contraseña es transformada mediante BCrypt antes de ser
     * almacenada en la base de datos, evitando guardar credenciales
     * en texto plano.
     * </p>
     *
     * @param usuario usuario a registrar
     * @return usuario registrado
     */
    public Usuario registrarUsuario(Usuario usuario) {

        usuario.setPassword(
                passwordEncoder.encode(usuario.getPassword())
        );

        return usuarioRepository.save(usuario);
    }

    /**
     * Autentica un usuario utilizando correo electrónico y contraseña.
     *
     * <p>
     * El proceso de autenticación consiste en:
     * </p>
     * <ol>
     *     <li>Buscar el usuario por correo electrónico.</li>
     *     <li>Comparar la contraseña ingresada con la almacenada.</li>
     *     <li>Retornar el usuario autenticado si las credenciales son válidas.</li>
     * </ol>
     *
     * @param email correo electrónico del usuario
     * @param password contraseña proporcionada por el usuario
     * @return usuario autenticado
     * @throws RuntimeException si el usuario no existe
     * @throws RuntimeException si la contraseña es incorrecta
     */
    public Usuario login(String email, String password) {

        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("Usuario no encontrado"));

        boolean ok = passwordEncoder.matches(
                password,
                usuario.getPassword()
        );

        if (!ok) {
            throw new RuntimeException("Contraseña incorrecta");
        }

        return usuario;
    }
}
