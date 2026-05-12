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

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final UsuarioService usuarioService;
    private final JwtService jwtService;

    public AuthController(UsuarioService usuarioService, JwtService jwtService) {
        this.usuarioService = usuarioService;
        this.jwtService = jwtService;
    }

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

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @Valid @RequestBody LoginRequest request
    ) {

        Usuario usuario = usuarioService.login(
                request.getEmail(),
                request.getPassword()
        );

        String token = jwtService.generateToken(
                usuario.getEmail()
        );

        return ResponseEntity.ok(
                new LoginResponse(token)
        );
    }
}
