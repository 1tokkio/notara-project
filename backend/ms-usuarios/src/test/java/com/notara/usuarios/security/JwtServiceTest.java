package com.notara.usuarios.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;

    private static final String SECRET =
            "hfebwuyfgeyiuwfiokwsfiujgewfnjwehfuhbvwehfudyjwegfyuwejkdfgweuif";
    private static final long EXPIRATION_MS = 900_000L;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secret", SECRET);
        ReflectionTestUtils.setField(jwtService, "expiration", EXPIRATION_MS);
        jwtService.debugSecret();
    }

    @Test
    @DisplayName("generateToken - retorna token no nulo para un email válido")
    void generateToken_retornaToken() {
        String token = jwtService.generateToken("user@test.com");
        assertNotNull(token);
        assertFalse(token.isBlank());
    }

    @Test
    @DisplayName("generateToken - el token contiene el email como subject")
    void generateToken_sujetoCorrecto() {
        String token = jwtService.generateToken("user@test.com");
        assertEquals("user@test.com", jwtService.extractEmail(token));
    }

    @Test
    @DisplayName("generateRefreshToken - retorna token distinto al access token")
    void generateRefreshToken_esDistinto() throws InterruptedException {
        String access  = jwtService.generateToken("user@test.com");
        Thread.sleep(10);
        String refresh = jwtService.generateRefreshToken("user@test.com");
        assertNotNull(refresh);
        assertNotEquals(access, refresh);
    }

    @Test
    @DisplayName("generateRefreshToken - subject coincide con el email")
    void generateRefreshToken_subjectCorrecto() {
        String token = jwtService.generateRefreshToken("refresh@test.com");
        assertEquals("refresh@test.com", jwtService.extractEmail(token));
    }

    @Test
    @DisplayName("isTokenValid - retorna true para token recién generado")
    void isTokenValid_tokenValido() {
        String token = jwtService.generateToken("valid@test.com");
        assertTrue(jwtService.isTokenValid(token));
    }

    @Test
    @DisplayName("isTokenValid - retorna false para token manipulado")
    void isTokenValid_tokenInvalido() {
        assertFalse(jwtService.isTokenValid("esto.no.es.un.jwt"));
    }

    @Test
    @DisplayName("isTokenValid - retorna false para token con firma incorrecta")
    void isTokenValid_firmaIncorrecta() {
        String token = jwtService.generateToken("user@test.com");
        assertFalse(jwtService.isTokenValid(token + "XYZ"));
    }

    @Test
    @DisplayName("isTokenValid - retorna false para token expirado")
    void isTokenValid_tokenExpirado() throws Exception {
        JwtService shortLived = new JwtService();
        ReflectionTestUtils.setField(shortLived, "secret", SECRET);
        ReflectionTestUtils.setField(shortLived, "expiration", 1L);
        shortLived.debugSecret();

        String token = shortLived.generateToken("expired@test.com");
        Thread.sleep(50);

        assertFalse(shortLived.isTokenValid(token));
    }

    @Test
    @DisplayName("extractEmail - extrae correctamente el email del token")
    void extractEmail_ok() {
        String email = "extract@test.com";
        String token = jwtService.generateToken(email);
        assertEquals(email, jwtService.extractEmail(token));
    }
}
