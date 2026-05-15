package com.notara.usuarios.controllers;

import com.notara.usuarios.dto.ProgresoDto;
import com.notara.usuarios.models.Progreso;
import com.notara.usuarios.services.ProgresoService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/progress")
public class ProgresoController {

    private final ProgresoService progresoService;

    public ProgresoController(ProgresoService progresoService) {
        this.progresoService = progresoService;
    }

    @GetMapping("/stats")
    public ResponseEntity<Progreso> getStats(Authentication authentication) {
        Progreso p = progresoService.getOrCreate(authentication.getName());
        return ResponseEntity.ok(p);
    }

    @PostMapping("/sync")
    public ResponseEntity<Progreso> sync(
            Authentication authentication,
            @RequestBody ProgresoDto dto
    ) {
        Progreso p = progresoService.sync(authentication.getName(), dto);
        return ResponseEntity.ok(p);
    }
}
