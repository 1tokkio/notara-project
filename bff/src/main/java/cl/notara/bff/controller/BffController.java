package cl.notara.bff.controller;

import cl.notara.bff.dto.DashboardDTO;
import cl.notara.bff.service.BffService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/bff")
public class BffController {

    private final BffService bffService;

    public BffController(BffService bffService) {
        this.bffService = bffService;
    }

    @GetMapping("/usuarios/{id}/dashboard")
    public ResponseEntity<DashboardDTO>
    obtenerDashboard(@PathVariable Long id) {

        return ResponseEntity.ok(
                bffService.obtenerDashboard(id)
        );
    }
}
