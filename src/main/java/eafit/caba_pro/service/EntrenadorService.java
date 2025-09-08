package eafit.caba_pro.service;

import eafit.caba_pro.model.Entrenador;
import eafit.caba_pro.repository.EntrenadorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class EntrenadorService {
    
    @Autowired
    private EntrenadorRepository entrenadorRepository;
    
    /**
     * Obtener todos los entrenadores
     */
    public List<Entrenador> findAll() {
        return entrenadorRepository.findAll();
    }
    
    /**
     * Obtener todos los entrenadores activos
     */
    public List<Entrenador> findAllActive() {
        return entrenadorRepository.findByActivoTrue();
    }
    
    /**
     * Buscar entrenador por ID
     */
    public Optional<Entrenador> findById(Long id) {
        return entrenadorRepository.findById(id);
    }
    
    /**
     * Buscar entrenador por documento
     */
    public Optional<Entrenador> findByDocumento(String documento) {
        return entrenadorRepository.findByDocumento(documento);
    }
    
    /**
     * Buscar entrenador por email
     */
    public Optional<Entrenador> findByEmail(String email) {
        return entrenadorRepository.findByEmail(email);
    }
    
    /**
     * Guardar un entrenador (crear o actualizar)
     */
    public Entrenador save(Entrenador entrenador) {
        // Validaciones adicionales antes de guardar
        validateEntrenador(entrenador);
        return entrenadorRepository.save(entrenador);
    }
    
    /**
     * Eliminar entrenador por ID (eliminación lógica)
     */
    public void deleteById(Long id) {
        Optional<Entrenador> entrenadorOpt = entrenadorRepository.findById(id);
        if (entrenadorOpt.isPresent()) {
            Entrenador entrenador = entrenadorOpt.get();
            entrenador.setActivo(false);
            entrenadorRepository.save(entrenador);
        } else {
            throw new RuntimeException("Entrenador con ID " + id + " no encontrado");
        }
    }
    
    /**
     * Eliminar entrenador definitivamente
     */
    public void hardDeleteById(Long id) {
        if (entrenadorRepository.existsById(id)) {
            entrenadorRepository.deleteById(id);
        } else {
            throw new RuntimeException("Entrenador con ID " + id + " no encontrado");
        }
    }
    
    /**
     * Buscar entrenadores por equipo
     */
    public List<Entrenador> findByEquipo(String equipo) {
        return entrenadorRepository.findByEquipoContainingIgnoreCase(equipo);
    }
    
    /**
     * Buscar entrenadores por categoría
     */
    public List<Entrenador> findByCategoria(Entrenador.Categoria categoria) {
        return entrenadorRepository.findByCategoria(categoria);
    }
    
    /**
     * Buscar entrenadores con experiencia mínima
     */
    public List<Entrenador> findEntrenadoresWithExperience(Integer minExperience) {
        return entrenadorRepository.findEntrenadoresWithExperience(minExperience);
    }
    
    /**
     * Buscar entrenadores por nombre completo
     */
    public List<Entrenador> findByNombreCompleto(String nombreCompleto) {
        return entrenadorRepository.findByNombreCompletoContaining(nombreCompleto);
    }
    
    /**
     * Contar entrenadores activos
     */
    public long countActiveEntrenadores() {
        return entrenadorRepository.countByActivoTrue();
    }
    
    /**
     * Contar entrenadores por equipo
     */
    public long countByEquipo(String equipo) {
        return entrenadorRepository.countByEquipo(equipo);
    }
    
    /**
     * Activar/Desactivar entrenador
     */
    public void toggleEntrenadorStatus(Long id) {
        Optional<Entrenador> entrenadorOpt = entrenadorRepository.findById(id);
        if (entrenadorOpt.isPresent()) {
            Entrenador entrenador = entrenadorOpt.get();
            entrenador.setActivo(!entrenador.getActivo());
            entrenadorRepository.save(entrenador);
        } else {
            throw new RuntimeException("Entrenador con ID " + id + " no encontrado");
        }
    }
    
    /**
     * Validaciones de negocio para entrenador
     */
    private void validateEntrenador(Entrenador entrenador) {
        // Verificar que el documento no esté duplicado
        if (entrenador.getId() == null) {
            // Es un entrenador nuevo
            if (entrenadorRepository.findByDocumento(entrenador.getDocumento()).isPresent()) {
                throw new RuntimeException("Ya existe un entrenador con el documento: " + entrenador.getDocumento());
            }
        } else {
            // Es un entrenador existente, verificar solo si cambió el documento
            Optional<Entrenador> existingEntrenador = entrenadorRepository.findById(entrenador.getId());
            if (existingEntrenador.isPresent() && 
                !existingEntrenador.get().getDocumento().equals(entrenador.getDocumento()) &&
                entrenadorRepository.findByDocumento(entrenador.getDocumento()).isPresent()) {
                throw new RuntimeException("Ya existe un entrenador con el documento: " + entrenador.getDocumento());
            }
        }
        
        // Verificar que el email no esté duplicado
        if (entrenador.getId() == null) {
            if (entrenadorRepository.findByEmail(entrenador.getEmail()).isPresent()) {
                throw new RuntimeException("Ya existe un entrenador con el email: " + entrenador.getEmail());
            }
        } else {
            Optional<Entrenador> existingEntrenador = entrenadorRepository.findById(entrenador.getId());
            if (existingEntrenador.isPresent() && 
                !existingEntrenador.get().getEmail().equals(entrenador.getEmail()) &&
                entrenadorRepository.findByEmail(entrenador.getEmail()).isPresent()) {
                throw new RuntimeException("Ya existe un entrenador con el email: " + entrenador.getEmail());
            }
        }
    }
    
    /**
     * Verificar si existe un entrenador
     */
    public boolean existsById(Long id) {
        return entrenadorRepository.existsById(id);
    }
}