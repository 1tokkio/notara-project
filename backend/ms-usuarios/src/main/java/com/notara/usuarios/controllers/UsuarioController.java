package com.notara.usuarios.controllers;

import com.notara.usuarios.models.Usuario;
import com.notara.usuarios.services.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.Authentication;

import java.util.List;

/**
 * Controlador REST encargado de la gestión de usuarios.
 *
 * <p>
 * Proporciona operaciones para crear, consultar, listar y eliminar
 * usuarios registrados en el sistema.
 * </p>
 *
 * <p>
 * Además, expone un endpoint que permite obtener la identidad
 * del usuario autenticado a través del token JWT.
 * </p>
 *
 * @author Notara
 * @version 1.0
 */
@RestController
@Tag(name = "Usuarios", description = "API de gestión de usuarios")
@RequestMapping("/usuarios")
public class UsuarioController {

    /**
     * Servicio encargado de la lógica de negocio relacionada con usuarios.
     */
    private final UsuarioService usuarioService;

    /**
     * Constructor que inyecta el servicio de usuarios.
     *
     * @param usuarioService servicio de gestión de usuarios
     */
    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    /**
     * Obtiene la lista completa de usuarios registrados.
     *
     * @return lista de usuarios
     */
    @Operation(summary = "Listar todos los usuarios")
    @GetMapping
    public ResponseEntity<List<Usuario>> listarUsuarios() {
        return ResponseEntity.ok(usuarioService.obtenerUsuarios());
    }

    /**
     * Crea un nuevo usuario en el sistema.
     *
     * <p>
     * Antes de guardar el usuario se validan los datos recibidos.
     * Si existen errores de validación, se devuelve una lista con
     * los mensajes correspondientes.
     * </p>
     *
     * @param usuario información del usuario a registrar
     * @param result resultado de las validaciones realizadas
     * @return usuario creado o lista de errores de validación
     */
    @Operation(summary = "Creador de usuario")
    @PostMapping
    public ResponseEntity<?> crearUsuario(
            @Valid @RequestBody Usuario usuario,
            BindingResult result
    ) {

        if (result.hasErrors()) {
            List<String> errores = result.getFieldErrors()
                    .stream()
                    .map(e -> e.getField() + ": " + e.getDefaultMessage())
                    .toList();

            return ResponseEntity.badRequest().body(errores);
        }

        Usuario nuevo = usuarioService.guardarUsuario(usuario);
        return ResponseEntity.ok(nuevo);
    }

    /**
     * Busca un usuario utilizando su identificador único.
     *
     * <p>
     * Si el usuario existe, se retorna con código HTTP 200.
     * En caso contrario, se devuelve HTTP 404 (Not Found).
     * </p>
     *
     * @param id identificador del usuario
     * @return usuario encontrado o respuesta 404
     */
    @Operation(summary = "Buscador por id de usuarios")
    @GetMapping("/{id}")
    public ResponseEntity<Usuario> obtenerUsuario(@PathVariable Long id) {
        return usuarioService.obtenerPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Elimina un usuario utilizando su identificador.
     *
     * <p>
     * Si la operación se realiza correctamente,
     * se retorna HTTP 204 (No Content).
     * </p>
     *
     * @param id identificador del usuario a eliminar
     * @return respuesta sin contenido
     */
    @Operation(summary = "Eliminador por id de usuario")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarUsuario(@PathVariable Long id) {
        usuarioService.eliminarUsuario(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Obtiene la identidad del usuario autenticado.
     *
     * <p>
     * El valor retornado corresponde al nombre de usuario o correo
     * electrónico contenido en el token JWT validado por Spring Security.
     * </p>
     *
     * @param authentication información de autenticación del usuario actual
     * @return identificador del usuario autenticado
     */
    @GetMapping("/me")
    public ResponseEntity<?> me(Authentication authentication) {

        return ResponseEntity.ok(
                authentication.getName()
        );
    }
}
