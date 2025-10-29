package eafit.caba_pro.controller.api;

import eafit.caba_pro.model.Arbitro;
import eafit.caba_pro.service.ArbitroService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/arbitros")
public class ArbitroApiController {

    @Autowired
    private ArbitroService arbitroService;

    // GET /api/arbitros - Obtener todos los árbitros
    @GetMapping
    public ResponseEntity<List<Arbitro>> getAllArbitros() {
        try {
            List<Arbitro> arbitros = arbitroService.findAll();
            return ResponseEntity.ok(arbitros);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // GET /api/arbitros/{id} - Obtener árbitro por ID
    @GetMapping("/{id}")
    public ResponseEntity<Arbitro> getArbitroById(@PathVariable Long id) {
        try {
            Optional<Arbitro> arbitro = arbitroService.findById(id);
            if (arbitro.isPresent()) {
                return ResponseEntity.ok(arbitro.get());
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // POST /api/arbitros - Crear nuevo árbitro
    @PostMapping
    public ResponseEntity<Arbitro> createArbitro(@RequestBody Arbitro arbitro) {
        try {
            // Validar datos básicos
            if (arbitro.getNombre() == null || arbitro.getNombre().trim().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            if (arbitro.getCedula() == null || arbitro.getCedula().trim().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }

            arbitroService.crearArbitro(arbitro);
            return ResponseEntity.status(HttpStatus.CREATED).body(arbitro);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // PUT /api/arbitros/{id} - Actualizar árbitro existente
    @PutMapping("/{id}")
    public ResponseEntity<Arbitro> updateArbitro(@PathVariable Long id, @RequestBody Arbitro arbitro) {
        try {
            Optional<Arbitro> arbitroExistente = arbitroService.findById(id);
            if (!arbitroExistente.isPresent()) {
                return ResponseEntity.notFound().build();
            }

            arbitro.setId(id); // Asegurar que el ID coincida
            // Usar el método de actualización existente con foto (null para no cambiar foto)
            Arbitro arbitroActualizado = arbitroService.updateArbitroWithPhoto(arbitro, null, false, false);
            return ResponseEntity.ok(arbitroActualizado);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // DELETE /api/arbitros/{id} - Eliminar árbitro
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteArbitro(@PathVariable Long id) {
        try {
            Optional<Arbitro> arbitro = arbitroService.findById(id);
            if (!arbitro.isPresent()) {
                return ResponseEntity.notFound().build();
            }

            boolean deleted = arbitroService.deleteById(id);
            if (deleted) {
                return ResponseEntity.noContent().build();
            } else {
                return ResponseEntity.badRequest().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // GET /api/arbitros/search?email={email} - Buscar árbitro por email
    @GetMapping("/search")
    public ResponseEntity<Arbitro> getArbitroByUsername(@RequestParam String username) {
        try {
            Optional<Arbitro> arbitro = arbitroService.findByUsername(username);
            if (arbitro.isPresent()) {
                return ResponseEntity.ok(arbitro.get());
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // GET /api/arbitros/cedula/{cedula} - Buscar árbitro por cédula
    @GetMapping("/cedula/{cedula}")
    public ResponseEntity<Arbitro> getArbitroByCedula(@PathVariable String cedula) {
        try {
            Optional<Arbitro> arbitro = arbitroService.findByCedula(cedula);
            if (arbitro.isPresent()) {
                return ResponseEntity.ok(arbitro.get());
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // GET /api/arbitros/{id}/photo - Obtener foto del árbitro
    @GetMapping("/{id}/photo")
    public ResponseEntity<byte[]> getArbitroPhoto(@PathVariable Long id) {
        try {
            return arbitroService.buildPhotoResponse(id);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
