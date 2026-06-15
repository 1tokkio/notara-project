package cl.notara.ms_vocabulario.controllers;

import cl.notara.ms_vocabulario.models.Categoria;
import cl.notara.ms_vocabulario.models.Ranking;
import cl.notara.ms_vocabulario.services.RankingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/vocabulario/ranking")
public class RankingController {

    private final RankingService service;

    public RankingController(RankingService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<Ranking>> global() {
        return ResponseEntity.ok(service.rankingGlobal());
    }

    @GetMapping("/categoria/{categoria}")
    public ResponseEntity<List<Ranking>> porCategoria(@PathVariable Categoria categoria) {
        return ResponseEntity.ok(service.rankingPorCategoria(categoria));
    }

    @GetMapping("/usuario/{idUsuario}")
    public ResponseEntity<List<Ranking>> usuario(@PathVariable Long idUsuario) {
        return ResponseEntity.ok(service.estadisticasUsuario(idUsuario));
    }
}
