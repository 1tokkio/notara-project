package com.notara.usuarios.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;

import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Servicio encargado de la gestión de tokens JWT (JSON Web Token).
 *
 * <p>
 * Proporciona funcionalidades para:
 * </p>
 * <ul>
 *     <li>Generar Access Tokens.</li>
 *     <li>Generar Refresh Tokens.</li>
 *     <li>Validar tokens.</li>
 *     <li>Extraer información contenida en los tokens.</li>
 * </ul>
 *
 * <p>
 * Los tokens son firmados utilizando el algoritmo HMAC SHA-256 (HS256)
 * y una clave secreta configurada en el archivo de propiedades de la aplicación.
 * </p>
 *
 * @author Notara
 * @version 1.0
 */
@Service
public class JwtService {

    /**
     * Clave secreta utilizada para firmar y validar los tokens JWT.
     */
    @Value("${jwt.secret}")
    private String secret;

    /**
     * Tiempo de expiración del Access Token en milisegundos.
     */
    @Value("${jwt.expiration-ms}")
    private long expiration;

    /**
     * Método ejecutado automáticamente después de la inicialización del bean.
     *
     * <p>
     * Muestra la clave secreta configurada en consola para propósitos
     * de depuración durante el desarrollo.
     * </p>
     *
     * <b>Importante:</b> En ambientes productivos se recomienda eliminar
     * este método para evitar exponer información sensible.
     */
    @PostConstruct
    public void debugSecret() {

        System.out.println("JWT SECRET:");
        System.out.println(secret);
    }

    /**
     * Genera la clave criptográfica utilizada para firmar y validar los tokens.
     *
     * @return clave secreta en formato {@link SecretKey}
     */
    private SecretKey getKey() {

        return Keys.hmacShaKeyFor(
                secret.getBytes(StandardCharsets.UTF_8)
        );
    }

    /**
     * Genera un Access Token JWT para el usuario indicado.
     *
     * <p>
     * El token contiene:
     * </p>
     * <ul>
     *     <li>Correo electrónico del usuario como subject.</li>
     *     <li>Fecha de emisión.</li>
     *     <li>Fecha de expiración.</li>
     * </ul>
     *
     * @param email correo electrónico del usuario autenticado
     * @return token JWT firmado
     */
    public String generateToken(String email) {

        Date now = new Date();

        Date expiry = new Date(
                now.getTime() + expiration
        );

        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(getKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Genera un Refresh Token JWT.
     *
     * <p>
     * Su duración es siete veces mayor que la del Access Token,
     * permitiendo solicitar nuevos tokens de acceso sin necesidad
     * de volver a autenticarse.
     * </p>
     *
     * @param email correo electrónico del usuario autenticado
     * @return refresh token firmado
     */
    public String generateRefreshToken(String email) {

        Date now = new Date();

        Date expiry = new Date(
                now.getTime() + expiration * 7L
        );

        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(getKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Extrae el correo electrónico almacenado en el token JWT.
     *
     * <p>
     * El correo se almacena como "subject" dentro del token.
     * </p>
     *
     * @param token token JWT válido
     * @return correo electrónico del usuario
     */
    public String extractEmail(String token) {

        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getKey())
                .build()
                .parseClaimsJws(token)
                .getBody();

        return claims.getSubject();
    }

    /**
     * Verifica si un token JWT es válido.
     *
     * <p>
     * La validación incluye:
     * </p>
     * <ul>
     *     <li>Firma digital correcta.</li>
     *     <li>Integridad de la información.</li>
     *     <li>No expiración del token.</li>
     * </ul>
     *
     * @param token token JWT a validar
     * @return {@code true} si el token es válido;
     *         {@code false} en caso contrario
     */
    public boolean isTokenValid(String token) {

        try {

            Jwts.parserBuilder()
                    .setSigningKey(getKey())
                    .build()
                    .parseClaimsJws(token);

            return true;

        } catch (Exception e) {
            return false;
        }
    }
}
