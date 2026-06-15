package com.notara.usuarios.controllers;

import com.notara.usuarios.dto.LoginRequest;
import com.notara.usuarios.dto.RegisterRequest;
import com.notara.usuarios.models.Usuario;
import com.notara.usuarios.services.UsuarioService;
import com.notara.usuarios.dto.LoginResponse;
import com.notara.usuarios.security.JwtService;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador encargado de la autenticación y registro de usuarios.
 *
 * <p>
 * Expone los endpoints públicos para:
 * </p>
 * <ul>
 *     <li>Registro de nuevos usuarios.</li>
 *     <li>Inicio de sesión mediante credenciales.</li>
 *     <li>Generación de tokens JWT para autenticación.</li>
 * </ul>
 *
 * <p>
 * Este controlador delega la lógica de negocio al servicio
 * {@link UsuarioService} y utiliza {@link JwtService}
 * para generar los tokens de acceso y actualización.
 * </p>
 *
 * @author Notara
 * @version 1.0
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    /**
     * Servicio encargado de la gestión de usuarios.
     */
    private final UsuarioService usuarioService;

    /**
     * Servicio encargado de generar y validar tokens JWT.
     */
    private final JwtService jwtService;

    /**
     * Constructor que inyecta las dependencias necesarias.
     *
     * @param usuarioService servicio de usuarios
     * @param jwtService servicio de generación de tokens JWT
     */
    public AuthController(UsuarioService usuarioService, JwtService jwtService) {
        this.usuarioService = usuarioService;
        this.jwtService = jwtService;
    }

    /**
     * Registra un nuevo usuario en el sistema.
     *
     * <p>
     * Recibe los datos enviados desde el cliente, crea una entidad
     * {@link Usuario} y delega el proceso de registro al servicio
     * correspondiente.
     * </p>
     *
     * @param request datos del usuario a registrar
     * @return usuario registrado
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(
            @Valid @RequestBody RegisterRequest request
    ) {

        Usuario usuario = new Usuario();

        usuario.setNombre(request.getNombre());
        usuario.setEmail(request.getEmail());
        usuario.setPassword(request.getPassword());

        Usuario nuevo = usuarioService.registrarUsuario(usuario);

        return ResponseEntity.ok(nuevo);
    }

    /**
     * Autentica a un usuario utilizando su correo electrónico
     * y contraseña.
     *
     * <p>
     * Si las credenciales son válidas:
     * </p>
     * <ul>
     *     <li>Genera un Access Token JWT.</li>
     *     <li>Genera un Refresh Token JWT.</li>
     *     <li>Retorna la información básica del usuario autenticado.</li>
     * </ul>
     *
     * @param request credenciales de inicio de sesión
     * @return respuesta con tokens JWT e información del usuario
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(
            @Valid @RequestBody LoginRequest request
    ) {

        Usuario usuario = usuarioService.login(
                request.getEmail(),
                request.getPassword()
        );

        String accessToken = jwtService.generateToken(usuario.getEmail());
        String refreshToken = jwtService.generateRefreshToken(usuario.getEmail());

        LoginResponse.UserInfo userInfo = new LoginResponse.UserInfo(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getEmail()
        );

        return ResponseEntity.ok(
                new LoginResponse(accessToken, refreshToken, userInfo)
        );
    }
}
