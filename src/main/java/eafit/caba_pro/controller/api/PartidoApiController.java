package eafit.caba_pro.controller.api;

import eafit.caba_pro.model.Partido;
import eafit.caba_pro.service.PartidoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/partidos")
public class PartidoApiController {

    @Autowired
    private PartidoService partidoService;

    // GET /api/partidos - Obtener todos los partidos
    @GetMapping
    public ResponseEntity<List<Partido>> getAllPartidos() {
        try {
            List<Partido> partidos = partidoService.findAll();
            return ResponseEntity.ok(partidos);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // GET /api/partidos/{id} - Obtener partido por ID
    @GetMapping("/{id}")
    public ResponseEntity<Partido> getPartidoById(@PathVariable Long id) {
        try {
            Optional<Partido> partido = partidoService.findById(id);
            if (partido.isPresent()) {
                return ResponseEntity.ok(partido.get());
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // POST /api/partidos - Crear nuevo partido
    @PostMapping
    public ResponseEntity<Partido> createPartido(@RequestBody Partido partido) {
        try {
            // Validar datos básicos
            if (partido.getFecha() == null) {
                return ResponseEntity.badRequest().build();
            }
            if (partido.getEquipoLocal() == null || partido.getEquipoVisitante() == null) {
                return ResponseEntity.badRequest().build();
            }
            if (partido.getEquipoLocal().getId().equals(partido.getEquipoVisitante().getId())) {
                return ResponseEntity.badRequest().build(); // No puede ser el mismo equipo
            }

            Partido nuevoPartido = partidoService.crearPartido(partido);
            return ResponseEntity.status(HttpStatus.CREATED).body(nuevoPartido);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // PUT /api/partidos/{id} - Actualizar partido existente
    @PutMapping("/{id}")
    public ResponseEntity<Partido> updatePartido(@PathVariable Long id, @RequestBody Partido partido) {
        try {
            Optional<Partido> partidoExistente = partidoService.findById(id);
            if (!partidoExistente.isPresent()) {
                return ResponseEntity.notFound().build();
            }

            partido.setId(id); // Asegurar que el ID coincida
            Partido partidoActualizado = partidoService.save(partido);
            return ResponseEntity.ok(partidoActualizado);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // DELETE /api/partidos/{id} - Eliminar partido
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePartido(@PathVariable Long id) {
        try {
            Optional<Partido> partido = partidoService.findById(id);
            if (!partido.isPresent()) {
                return ResponseEntity.notFound().build();
            }

            boolean deleted = partidoService.deleteById(id);
            if (deleted) {
                return ResponseEntity.noContent().build();
            } else {
                return ResponseEntity.badRequest().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // GET /api/partidos/estado/{estado} - Obtener partidos por estado
    @GetMapping("/estado/{estado}")
    public ResponseEntity<List<Partido>> getPartidosByEstado(@PathVariable String estado) {
        try {
            Partido.EstadoPartido estadoEnum = Partido.EstadoPartido.valueOf(estado.toUpperCase());
            List<Partido> partidos = partidoService.findAll().stream()
                    .filter(p -> p.getEstado() == estadoEnum)
                    .collect(java.util.stream.Collectors.toList());
            return ResponseEntity.ok(partidos);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // GET /api/partidos/equipo/{equipoNombre} - Obtener partidos de un equipo por nombre
    @GetMapping("/equipo/{equipoNombre}")
    public ResponseEntity<List<Partido>> getPartidosByEquipo(@PathVariable String equipoNombre) {
        try {
            List<Partido> partidos = partidoService.findPartidosProgramadosByEquipo(equipoNombre);
            return ResponseEntity.ok(partidos);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // GET /api/partidos/sin-arbitro - Obtener partidos sin árbitro asignado
    @GetMapping("/sin-arbitro")
    public ResponseEntity<List<Partido>> getPartidosSinArbitro() {
        try {
            List<Partido> partidos = partidoService.getPartidosSinArbitro();
            return ResponseEntity.ok(partidos);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
