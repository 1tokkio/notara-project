package cl.notara.ms_pagos_subscripciones.controllers;

import cl.notara.ms_pagos_subscripciones.models.Suscripcion;
import cl.notara.ms_pagos_subscripciones.services.SuscripcionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/suscripciones")
public class SuscripcionController {

    private final SuscripcionService service;

    public SuscripcionController(SuscripcionService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<Suscripcion>> listar() {
        return ResponseEntity.ok(service.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Suscripcion> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(service.obtener(id));
    }

    @GetMapping("/usuario/{idUsuario}")
    public ResponseEntity<List<Suscripcion>> listarPorUsuario(@PathVariable Long idUsuario) {
        return ResponseEntity.ok(service.listarPorUsuario(idUsuario));
    }

    @PostMapping
    public ResponseEntity<Suscripcion> crear(@Valid @RequestBody Suscripcion suscripcion) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(suscripcion));
    }

    @PutMapping("/{id}/cancelar")
    public ResponseEntity<Suscripcion> cancelar(@PathVariable Long id) {
        return ResponseEntity.ok(service.cancelar(id));
    }

    @PutMapping("/{id}/renovar")
    public ResponseEntity<Suscripcion> renovar(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        LocalDate nuevaFechaFin = LocalDate.parse(body.get("fechaFin"));
        return ResponseEntity.ok(service.renovar(id, nuevaFechaFin));
    }
}
