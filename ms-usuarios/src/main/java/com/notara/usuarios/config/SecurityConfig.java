package com.notara.usuarios.config;

import com.notara.usuarios.security.JwtFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Configuración de seguridad de la aplicación.
 *
 * <p>
 * Esta clase define las reglas de autenticación y autorización mediante
 * Spring Security. Se encarga de:
 * </p>
 *
 * <ul>
 *     <li>Registrar el filtro JWT para validar los tokens enviados en las peticiones.</li>
 *     <li>Permitir el acceso público a los endpoints de autenticación y documentación.</li>
 *     <li>Proteger el resto de los recursos exigiendo autenticación.</li>
 *     <li>Configurar el codificador de contraseñas BCrypt.</li>
 * </ul>
 *
 * @author Notara
 * @version 1.0
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Filtro encargado de validar los tokens JWT
     * presentes en las solicitudes HTTP.
     */
    private final JwtFilter jwtFilter;

    /**
     * Constructor que inyecta el filtro JWT.
     *
     * @param jwtFilter filtro encargado de la validación de tokens JWT
     */
    public SecurityConfig(JwtFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    /**
     * Registra un codificador de contraseñas BCrypt como Bean de Spring.
     *
     * <p>
     * BCrypt permite almacenar contraseñas de forma segura mediante hashing
     * con salt automático y múltiples rondas de cifrado.
     * </p>
     *
     * @return instancia de {@link BCryptPasswordEncoder}
     */
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Configura la cadena de filtros de seguridad de Spring Security.
     *
     * <p>
     * La configuración realiza las siguientes acciones:
     * </p>
     *
     * <ul>
     *     <li>Deshabilita la protección CSRF para facilitar el uso de APIs REST.</li>
     *     <li>Permite el acceso sin autenticación a:
     *          <ul>
     *              <li>/auth/**</li>
     *              <li>/swagger-ui/**</li>
     *              <li>/v3/api-docs/**</li>
     *          </ul>
     *     </li>
     *     <li>Exige autenticación para cualquier otro endpoint.</li>
     *     <li>Agrega el filtro JWT antes del filtro de autenticación estándar de Spring.</li>
     *     <li>Habilita autenticación HTTP Basic para pruebas o compatibilidad.</li>
     * </ul>
     *
     * @param http objeto de configuración de seguridad HTTP
     * @return cadena de filtros de seguridad configurada
     * @throws Exception si ocurre un error durante la configuración
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        http
                .csrf(csrf -> csrf.disable())

                .authorizeHttpRequests(auth -> auth

                        .requestMatchers(
                                "/auth/**",
                                "/swagger-ui/**",
                                "/v3/api-docs/**"
                        ).permitAll()

                        .requestMatchers(HttpMethod.GET, "/usuarios/**").permitAll()

                        .anyRequest().authenticated()
                )

                .addFilterBefore(
                        jwtFilter,
                        UsernamePasswordAuthenticationFilter.class
                )

                .httpBasic(httpBasic -> {});

        return http.build();
    }
}
