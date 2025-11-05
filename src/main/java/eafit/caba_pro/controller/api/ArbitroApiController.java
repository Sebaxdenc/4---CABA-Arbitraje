// Updated ArbitroApiController.java
package eafit.caba_pro.controller.api;

import eafit.caba_pro.dto.ArbitroDTO;
import eafit.caba_pro.model.Arbitro;
import eafit.caba_pro.service.ArbitroService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/arbitros")
public class ArbitroApiController {

    @Autowired
    private ArbitroService arbitroService;

    // GET /api/arbitros - Obtener todos los árbitros
    @GetMapping
    public ResponseEntity<List<ArbitroDTO>> getAllArbitros() {
        try {
            List<Arbitro> arbitros = arbitroService.findAll();
            List<ArbitroDTO> arbitroDTOs = arbitros.stream()
                    .map(ArbitroDTO::fromEntity)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(arbitroDTOs);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // GET /api/arbitros/{id} - Obtener árbitro por ID
    @GetMapping("/{id}")
    public ResponseEntity<ArbitroDTO> getArbitroById(@PathVariable Long id) {
        try {
            Optional<Arbitro> arbitro = arbitroService.findById(id);
            if (arbitro.isPresent()) {
                return ResponseEntity.ok(ArbitroDTO.fromEntity(arbitro.get()));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // POST /api/arbitros - Crear nuevo árbitro
    @PostMapping
    public ResponseEntity<ArbitroDTO> createArbitro(@RequestBody ArbitroDTO arbitroDTO) {
        try {
            // Validar datos básicos
            if (arbitroDTO.getNombre() == null || arbitroDTO.getNombre().trim().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            if (arbitroDTO.getCedula() == null || arbitroDTO.getCedula().trim().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }

            Arbitro arbitro = arbitroDTO.toEntity();
            arbitroService.crearArbitro(arbitro);
            return ResponseEntity.status(HttpStatus.CREATED).body(ArbitroDTO.fromEntity(arbitro));
        } catch (Exception e) {
            System.out.println(e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // PUT /api/arbitros/{id} - Actualizar árbitro existente
    @PutMapping("/{id}")
    public ResponseEntity<ArbitroDTO> updateArbitro(@PathVariable Long id, @RequestBody ArbitroDTO arbitroDTO) {
        try {
            Optional<Arbitro> arbitroExistente = arbitroService.findById(id);
            if (!arbitroExistente.isPresent()) {
                return ResponseEntity.notFound().build();
            }

            Arbitro arbitro = arbitroDTO.toEntity();
            arbitro.setId(id);
            Arbitro arbitroActualizado = arbitroService.updateArbitroWithPhoto(arbitro, null, false, false);
            return ResponseEntity.ok(ArbitroDTO.fromEntity(arbitroActualizado));
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

    // GET /api/arbitros/search?username={username} - Buscar árbitro por username
    @GetMapping("/search")
    public ResponseEntity<ArbitroDTO> getArbitroByUsername(@RequestParam String username) {
        try {
            Optional<Arbitro> arbitro = arbitroService.findByUsername(username);
            if (arbitro.isPresent()) {
                return ResponseEntity.ok(ArbitroDTO.fromEntity(arbitro.get()));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // GET /api/arbitros/cedula/{cedula} - Buscar árbitro por cédula
    @GetMapping("/cedula/{cedula}")
    public ResponseEntity<ArbitroDTO> getArbitroByCedula(@PathVariable String cedula) {
        try {
            Optional<Arbitro> arbitro = arbitroService.findByCedula(cedula);
            if (arbitro.isPresent()) {
                return ResponseEntity.ok(ArbitroDTO.fromEntity(arbitro.get()));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}