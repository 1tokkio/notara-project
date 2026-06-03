package com.notara.usuarios.controllers;

import com.notara.usuarios.dto.ProgresoDto;
import com.notara.usuarios.models.Progreso;
import com.notara.usuarios.services.ProgresoService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador encargado de la gestión del progreso de los usuarios.
 *
 * <p>
 * Proporciona endpoints para consultar y sincronizar las estadísticas
 * y el progreso asociado a un usuario autenticado dentro de la plataforma.
 * </p>
 *
 * <p>
 * La identificación del usuario se obtiene a través del objeto
 * {@link Authentication}, el cual contiene la información extraída
 * del token JWT validado por Spring Security.
 * </p>
 *
 * @author Notara
 * @version 1.0
 */
@RestController
@RequestMapping("/progress")
public class ProgresoController {

    /**
     * Servicio encargado de la lógica de negocio relacionada con el progreso.
     */
    private final ProgresoService progresoService;

    /**
     * Constructor que inyecta el servicio de progreso.
     *
     * @param progresoService servicio de gestión de progreso
     */
    public ProgresoController(ProgresoService progresoService) {
        this.progresoService = progresoService;
    }

    /**
     * Obtiene las estadísticas y el progreso del usuario autenticado.
     *
     * <p>
     * Si el usuario aún no posee un registro de progreso, se crea
     * automáticamente mediante el servicio correspondiente.
     * </p>
     *
     * @param authentication información del usuario autenticado
     * @return progreso actual del usuario
     */
    @GetMapping("/stats")
    public ResponseEntity<Progreso> getStats(Authentication authentication) {
        Progreso p = progresoService.getOrCreate(authentication.getName());
        return ResponseEntity.ok(p);
    }

    /**
     * Sincroniza el progreso del usuario autenticado con los datos
     * enviados desde el cliente.
     *
     * <p>
     * Este endpoint permite actualizar las estadísticas almacenadas
     * en el servidor utilizando la información recibida en el objeto
     * {@link ProgresoDto}.
     * </p>
     *
     * @param authentication información del usuario autenticado
     * @param dto datos de progreso enviados por el cliente
     * @return progreso actualizado del usuario
     */
    @PostMapping("/sync")
    public ResponseEntity<Progreso> sync(
            Authentication authentication,
            @RequestBody ProgresoDto dto
    ) {
        Progreso p = progresoService.sync(authentication.getName(), dto);
        return ResponseEntity.ok(p);
    }
}
