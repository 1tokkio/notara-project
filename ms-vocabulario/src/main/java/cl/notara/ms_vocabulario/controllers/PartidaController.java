package cl.notara.ms_vocabulario.controllers;

import cl.notara.ms_vocabulario.dto.*;
import cl.notara.ms_vocabulario.models.Partida;
import cl.notara.ms_vocabulario.services.PartidaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/vocabulario/partidas")
public class PartidaController {

    private final PartidaService service;

    public PartidaController(PartidaService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<PreguntaDTO> iniciar(@Valid @RequestBody IniciarPartidaRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.iniciar(req));
    }

    @GetMapping("/{id}/pregunta")
    public ResponseEntity<PreguntaDTO> preguntaActual(@PathVariable Long id) {
        return ResponseEntity.ok(service.obtenerPreguntaActual(id));
    }

    @PostMapping("/{id}/responder")
    public ResponseEntity<RespuestaDTO> responder(
            @PathVariable Long id,
            @Valid @RequestBody ResponderRequest req) {
        return ResponseEntity.ok(service.responder(id, req));
    }

    @PutMapping("/{id}/abandonar")
    public ResponseEntity<Void> abandonar(@PathVariable Long id) {
        service.abandonar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Partida> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(service.obtener(id));
    }

    @GetMapping("/usuario/{idUsuario}")
    public ResponseEntity<List<Partida>> historial(@PathVariable Long idUsuario) {
        return ResponseEntity.ok(service.historialUsuario(idUsuario));
    }
}
