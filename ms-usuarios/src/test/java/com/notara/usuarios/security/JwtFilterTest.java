package com.notara.usuarios.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtFilterTest {

    private JwtService jwtService;
    private JwtFilter  jwtFilter;

    @BeforeEach
    void setUp() {
        jwtService = mock(JwtService.class);
        jwtFilter  = new JwtFilter(jwtService);
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("doFilterInternal - sin header Authorization pasa la petición sin autenticar")
    void sinHeader_pasaFiltro() throws Exception {
        HttpServletRequest  req   = mock(HttpServletRequest.class);
        HttpServletResponse res   = mock(HttpServletResponse.class);
        FilterChain         chain = mock(FilterChain.class);

        when(req.getHeader("Authorization")).thenReturn(null);

        jwtFilter.doFilterInternal(req, res, chain);

        verify(chain).doFilter(req, res);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    @DisplayName("doFilterInternal - header sin 'Bearer ' pasa el filtro sin autenticar")
    void headerSinBearer_pasaFiltro() throws Exception {
        HttpServletRequest  req   = mock(HttpServletRequest.class);
        HttpServletResponse res   = mock(HttpServletResponse.class);
        FilterChain         chain = mock(FilterChain.class);

        when(req.getHeader("Authorization")).thenReturn("Basic dXNlcjpwYXNz");

        jwtFilter.doFilterInternal(req, res, chain);

        verify(chain).doFilter(req, res);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    @DisplayName("doFilterInternal - token inválido pasa el filtro sin autenticar")
    void tokenInvalido_pasaFiltro() throws Exception {
        HttpServletRequest  req   = mock(HttpServletRequest.class);
        HttpServletResponse res   = mock(HttpServletResponse.class);
        FilterChain         chain = mock(FilterChain.class);

        when(req.getHeader("Authorization")).thenReturn("Bearer token.invalido");
        when(jwtService.isTokenValid("token.invalido")).thenReturn(false);

        jwtFilter.doFilterInternal(req, res, chain);

        verify(chain).doFilter(req, res);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    @DisplayName("doFilterInternal - token válido establece autenticación en el contexto")
    void tokenValido_autenticaUsuario() throws Exception {
        HttpServletRequest  req   = mock(HttpServletRequest.class);
        HttpServletResponse res   = mock(HttpServletResponse.class);
        FilterChain         chain = mock(FilterChain.class);

        when(req.getHeader("Authorization")).thenReturn("Bearer token.valido.aqui");
        when(jwtService.isTokenValid("token.valido.aqui")).thenReturn(true);
        when(jwtService.extractEmail("token.valido.aqui")).thenReturn("user@test.com");

        jwtFilter.doFilterInternal(req, res, chain);

        verify(chain).doFilter(req, res);
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals("user@test.com",
                SecurityContextHolder.getContext().getAuthentication().getName());
    }
}
