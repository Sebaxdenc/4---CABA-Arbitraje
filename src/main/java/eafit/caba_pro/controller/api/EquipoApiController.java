package eafit.caba_pro.controller.api;

import eafit.caba_pro.model.Equipo;
import eafit.caba_pro.service.EquipoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/equipos")
public class EquipoApiController {

    @Autowired
    private EquipoService equipoService;

    // GET /api/equipos - Obtener todos los equipos
    @GetMapping
    public ResponseEntity<List<Equipo>> getAllEquipos() {
        try {
            List<Equipo> equipos = equipoService.findAll();
            return ResponseEntity.ok(equipos);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // GET /api/equipos/{id} - Obtener equipo por ID
    @GetMapping("/{id}")
    public ResponseEntity<Equipo> getEquipoById(@PathVariable Long id) {
        try {
            Optional<Equipo> equipo = equipoService.findById(id);
            if (equipo.isPresent()) {
                return ResponseEntity.ok(equipo.get());
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // POST /api/equipos - Crear nuevo equipo
    @PostMapping
    public ResponseEntity<Equipo> createEquipo(@RequestBody Equipo equipo) {
        try {
            // Validar datos básicos
            if (equipo.getNombre() == null || equipo.getNombre().trim().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }

            equipoService.createTeam(equipo);
            return ResponseEntity.status(HttpStatus.CREATED).body(equipo);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // PUT /api/equipos/{id} - Actualizar equipo existente
    @PutMapping("/{id}")
    public ResponseEntity<Equipo> updateEquipo(@PathVariable Long id, @RequestBody Equipo equipo) {
        try {
            Optional<Equipo> equipoExistente = equipoService.findById(id);
            if (!equipoExistente.isPresent()) {
                return ResponseEntity.notFound().build();
            }

            equipo.setId(id); // Asegurar que el ID coincida
            equipoService.save(equipo);
            return ResponseEntity.ok(equipo);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // DELETE /api/equipos/{id} - Eliminar equipo (nota: EquipoService no tiene método delete)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEquipo(@PathVariable Long id) {
        // EquipoService no tiene método delete implementado
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    // GET /api/equipos/search?nombre={nombre} - Buscar equipos por nombre
    @GetMapping("/search")
    public ResponseEntity<List<Equipo>> getEquiposByNombre(@RequestParam String nombre) {
        try {
            // EquipoService no tiene método de búsqueda por nombre, simular con findAll()
            List<Equipo> todosEquipos = equipoService.findAll();
            List<Equipo> equiposFiltrados = todosEquipos.stream()
                    .filter(equipo -> equipo.getNombre() != null && 
                            equipo.getNombre().toLowerCase().contains(nombre.toLowerCase()))
                    .collect(java.util.stream.Collectors.toList());
            return ResponseEntity.ok(equiposFiltrados);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
