package eafit.caba_pro.controller.api;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import eafit.caba_pro.model.Partido;
import eafit.caba_pro.model.Torneo;
import eafit.caba_pro.service.TorneoService;

@RestController
@RequestMapping("/api/torneos")
public class TorneoApiController {

    private final TorneoService torneoService;

    public TorneoApiController(TorneoService torneoService) {
        this.torneoService = torneoService;
    }

    @GetMapping
    public ResponseEntity<List<Torneo>> listAll() {
        return ResponseEntity.ok(torneoService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Torneo> getById(@PathVariable Long id) {
        Optional<Torneo> opt = torneoService.findById(id);
        return opt.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Map<String, Object> payload) {
        try {
            // Mapeo simple (puedes cambiar por DTO + validación)
            Torneo torneo = new Torneo();
            torneo.setNombre((String) payload.get("nombre"));
            torneo.setSede((String) payload.get("sede"));
            @SuppressWarnings("unchecked")
            List<Integer> ids = (List<Integer>) payload.get("partidoIds");
            List<Long> partidoIds = ids.stream().map(Integer::longValue).toList();

            Torneo creado = torneoService.crearConPartidos(torneo, partidoIds);
            return ResponseEntity.status(HttpStatus.CREATED).body(creado);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Torneo cambios) {
        try {
            Torneo t = torneoService.update(id, cambios);
            return ResponseEntity.ok(t);
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", ex.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try {
            torneoService.delete(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/{id}/asignar-partido")
    public ResponseEntity<?> asignarPartido(@PathVariable Long id, @RequestParam Long partidoId) {
        try {
            torneoService.asignarPartido(id, partidoId);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", ex.getMessage()));
        }
    }

    @PostMapping("/{id}/quitar-partido")
    public ResponseEntity<?> quitarPartido(@PathVariable Long id, @RequestParam Long partidoId) {
        try {
            torneoService.quitarPartido(id, partidoId);
            return ResponseEntity.ok().build();
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", ex.getMessage()));
        }
    }

    @GetMapping("/{id}/partidos")
    public ResponseEntity<List<Partido>> listarPartidos(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(torneoService.listarPartidosDeTorneo(id));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

}