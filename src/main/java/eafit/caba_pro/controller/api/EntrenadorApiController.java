package eafit.caba_pro.controller.api;

import eafit.caba_pro.model.Entrenador;
import eafit.caba_pro.service.EntrenadorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/entrenadores")
public class EntrenadorApiController {

    @Autowired
    private EntrenadorService entrenadorService;

    // GET /api/entrenadores - Obtener todos los entrenadores
    @GetMapping
    public ResponseEntity<List<Entrenador>> getAllEntrenadores() {
        try {
            List<Entrenador> entrenadores = entrenadorService.findAllActive();
            return ResponseEntity.ok(entrenadores);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // GET /api/entrenadores/{id} - Obtener entrenador por ID
    @GetMapping("/{id}")
    public ResponseEntity<Entrenador> getEntrenadorById(@PathVariable Long id) {
        try {
            Optional<Entrenador> entrenador = entrenadorService.findById(id);
            if (entrenador.isPresent()) {
                return ResponseEntity.ok(entrenador.get());
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // POST /api/entrenadores - Crear nuevo entrenador
    @PostMapping
    public ResponseEntity<Entrenador> createEntrenador(@RequestBody Entrenador entrenador) {
        try {
            // Validar datos básicos
            if (entrenador.getNombreCompleto() == null || entrenador.getNombreCompleto().trim().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            if (entrenador.getEmail() == null || entrenador.getEmail().trim().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }

            Entrenador nuevoEntrenador = entrenadorService.createCoachWithUser(entrenador);
            return ResponseEntity.status(HttpStatus.CREATED).body(nuevoEntrenador);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // PUT /api/entrenadores/{id} - Actualizar entrenador existente
    @PutMapping("/{id}")
    public ResponseEntity<Entrenador> updateEntrenador(@PathVariable Long id, @RequestBody Entrenador entrenador) {
        try {
            Optional<Entrenador> entrenadorExistente = entrenadorService.findById(id);
            if (!entrenadorExistente.isPresent()) {
                return ResponseEntity.notFound().build();
            }

            entrenador.setId(id); // Asegurar que el ID coincida
            Entrenador entrenadorActualizado = entrenadorService.updateCoach(entrenador);
            return ResponseEntity.ok(entrenadorActualizado);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // DELETE /api/entrenadores/{id} - Eliminar entrenador
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEntrenador(@PathVariable Long id) {
        try {
            Optional<Entrenador> entrenador = entrenadorService.findById(id);
            if (!entrenador.isPresent()) {
                return ResponseEntity.notFound().build();
            }

            boolean deleted = entrenadorService.deleteById(id);
            if (deleted) {
                return ResponseEntity.noContent().build();
            } else {
                return ResponseEntity.badRequest().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // GET /api/entrenadores/search?email={email} - Buscar entrenador por email
    @GetMapping("/search")
    public ResponseEntity<Entrenador> getEntrenadorByEmail(@RequestParam String email) {
        try {
            Optional<Entrenador> entrenador = entrenadorService.findByEmail(email);
            if (entrenador.isPresent()) {
                return ResponseEntity.ok(entrenador.get());
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // GET /api/entrenadores/cedula/{cedula} - Buscar entrenador por cédula
    @GetMapping("/cedula/{cedula}")
    public ResponseEntity<Entrenador> getEntrenadorByCedula(@PathVariable String cedula) {
        try {
            Optional<Entrenador> entrenador = entrenadorService.findByCedula(cedula);
            if (entrenador.isPresent()) {
                return ResponseEntity.ok(entrenador.get());
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
