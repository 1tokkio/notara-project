package com.notara.usuarios.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Filtro de autenticación basado en JSON Web Token (JWT).
 *
 * <p>
 * Este filtro intercepta todas las solicitudes HTTP entrantes y verifica
 * la presencia de un token JWT válido en el encabezado Authorization.
 * </p>
 *
 * <p>
 * Si el token es válido, se extrae la identidad del usuario y se registra
 * en el contexto de seguridad de Spring Security, permitiendo que los
 * recursos protegidos puedan identificar al usuario autenticado.
 * </p>
 *
 * <p>
 * La clase extiende {@link OncePerRequestFilter}, garantizando que el filtro
 * se ejecute una única vez por cada solicitud HTTP.
 * </p>
 *
 * @author Notara
 * @version 1.0
 */
@Component
public class JwtFilter extends OncePerRequestFilter {

    /**
     * Servicio encargado de la generación, validación
     * y extracción de información desde los tokens JWT.
     */
    private final JwtService jwtService;

    /**
     * Constructor que inyecta el servicio JWT.
     *
     * @param jwtService servicio encargado de gestionar tokens JWT
     */
    public JwtFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    /**
     * Procesa cada solicitud HTTP para verificar la autenticación
     * mediante un token JWT.
     *
     * <p>
     * Flujo de ejecución:
     * </p>
     * <ol>
     *     <li>Obtiene el encabezado Authorization.</li>
     *     <li>Verifica que exista y comience con "Bearer ".</li>
     *     <li>Extrae el token JWT.</li>
     *     <li>Valida la autenticidad y vigencia del token.</li>
     *     <li>Obtiene el correo electrónico del usuario autenticado.</li>
     *     <li>Crea un objeto Authentication para Spring Security.</li>
     *     <li>Registra la autenticación en SecurityContextHolder.</li>
     *     <li>Continúa la cadena de filtros.</li>
     * </ol>
     *
     * <p>
     * Si no existe un token o este es inválido, la solicitud continúa
     * sin autenticación y será evaluada posteriormente por las reglas
     * de seguridad configuradas.
     * </p>
     *
     * @param request solicitud HTTP entrante
     * @param response respuesta HTTP saliente
     * @param filterChain cadena de filtros de Spring Security
     * @throws ServletException si ocurre un error durante el filtrado
     * @throws IOException si ocurre un error de entrada o salida
     */
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String authHeader =
                request.getHeader("Authorization");

        if (
                authHeader == null ||
                        !authHeader.startsWith("Bearer ")
        ) {

            filterChain.doFilter(request, response);
            return;
        }

        String token =
                authHeader.substring(7);

        if (!jwtService.isTokenValid(token)) {

            filterChain.doFilter(request, response);
            return;
        }

        String email =
                jwtService.extractEmail(token);

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        email,
                        null,
                        List.of(
                                new SimpleGrantedAuthority("USER")
                        )
                );

        SecurityContextHolder
                .getContext()
                .setAuthentication(authentication);

        filterChain.doFilter(request, response);
    }
}
