package eafit.caba_pro.service;

import eafit.caba_pro.model.Entrenador;
import eafit.caba_pro.model.Usuario;
import eafit.caba_pro.model.Reseña;
import eafit.caba_pro.repository.EntrenadorRepository;
import eafit.caba_pro.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import eafit.caba_pro.repository.ReseñaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class EntrenadorService {

    @Autowired
    private EntrenadorRepository entrenadorRepository;
    
    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ReseñaRepository reseñaRepository;

    /**
     * Métodos básicos CRUD
     */
    public List<Entrenador> findAll() {
        return entrenadorRepository.findAll();
    }

    public List<Entrenador> findAllActive() {
        return entrenadorRepository.findByActivoTrue();
    }

    public Optional<Entrenador> findById(Long id) {
        return entrenadorRepository.findById(id);
    }

    public Entrenador save(Entrenador entrenador) {
        if (entrenador.getId() == null) {
            entrenador.setFechaCreacion(LocalDateTime.now());
        }
        entrenador.setFechaActualizacion(LocalDateTime.now());
        return entrenadorRepository.save(entrenador);
    }

    public boolean deleteById(Long id) {
        try {
            Optional<Entrenador> entrenadorOpt = entrenadorRepository.findById(id);
            if (entrenadorOpt.isPresent()) {
                Entrenador entrenador = entrenadorOpt.get();
                entrenador.setActivo(false); // Soft delete
                entrenador.setFechaActualizacion(LocalDateTime.now());
                entrenadorRepository.save(entrenador);
                return true;
            }
            return false;
        } catch (Exception e) {
            throw new RuntimeException("Error al eliminar entrenador: " + e.getMessage());
        }
    }

    /**
     * Métodos de búsqueda específicos
     */
    public List<Entrenador> findByNombreCompleto(String nombre) {
        return entrenadorRepository.findByNombreCompletoContainingIgnoreCaseAndActivoTrue(nombre);
    }

    public List<Entrenador> findByEquipo(String equipo) {
        return entrenadorRepository.findByEquipoContainingIgnoreCaseAndActivoTrue(equipo);
    }

    public List<Entrenador> findByCategoria(Entrenador.Categoria categoria) {
        return entrenadorRepository.findByCategoriaAndActivoTrue(categoria);
    }

    public Optional<Entrenador> findByUsuarioUsername(String username) {
        return entrenadorRepository.findByUsuario_UsernameAndActivoTrue(username);
    }

    public Optional<Entrenador> findByCedula(String cedula) {
        return entrenadorRepository.findByCedulaAndActivoTrue(cedula);
    }

    public Optional<Entrenador> findByEmail(String email) {
        return entrenadorRepository.findByEmailAndActivoTrue(email);
    }

    /**
     * Métodos de validación
     */
    public boolean existsByCedula(String cedula) {
        return entrenadorRepository.existsByCedulaAndActivoTrue(cedula);
    }

    public boolean existsByEmail(String email) {
        return entrenadorRepository.existsByEmailAndActivoTrue(email);
    }

    public boolean existsByUsuarioUsername(String username) {
        return entrenadorRepository.existsByUsuario_UsernameAndActivoTrue(username);
    }

    /**
     * Métodos de conteo
     */
    public long count() {
        return entrenadorRepository.count();
    }

    public long countActive() {
        return entrenadorRepository.countByActivoTrue();
    }

    public long countByCategoria(Entrenador.Categoria categoria) {
        return entrenadorRepository.countByCategoriaAndActivoTrue(categoria);
    }

    /**
     * Crear entrenador con usuario asociado (para admin)
     */
    public Entrenador createCoachWithUser(Entrenador entrenador) {
        try {
            // Validar que no exista la cédula
            if (existsByCedula(entrenador.getCedula())) {
                throw new RuntimeException("Ya existe un entrenador con la cédula: " + entrenador.getCedula());
            }

            // Validar que no exista el email
            if (existsByEmail(entrenador.getEmail())) {
                throw new RuntimeException("Ya existe un entrenador con el email: " + entrenador.getEmail());
            }

            // Crear usuario para el entrenador
            Usuario usuario = new Usuario();
            usuario.setUsername(entrenador.getCedula()); // Username = cédula
            usuario.setEmail(entrenador.getEmail());
            usuario.setPassword(entrenador.getCedula()); // Password inicial = cédula
            usuario.setRole("COACH");
            usuario.setActivo(true);
            usuario.setFechaCreacion(LocalDateTime.now());
            
            // Guardar usuario
            Usuario savedUsuario = usuarioRepository.save(usuario);

            // Asociar usuario al entrenador
            entrenador.setUsuario(savedUsuario);
            entrenador.setActivo(true);
            entrenador.setFechaCreacion(LocalDateTime.now());
            entrenador.setFechaActualizacion(LocalDateTime.now());

            return entrenadorRepository.save(entrenador);

        } catch (Exception e) {
            throw new RuntimeException("Error al crear entrenador con usuario: " + e.getMessage());
        }
    }

    /**
     * Actualizar entrenador (preservando usuario)
     */
    public Entrenador updateCoach(Entrenador entrenadorActualizado) {
        try {
            Optional<Entrenador> existingOpt = entrenadorRepository.findById(entrenadorActualizado.getId());
            
            if (existingOpt.isEmpty()) {
                throw new RuntimeException("Entrenador no encontrado");
            }

            Entrenador existing = existingOpt.get();
            
            // Actualizar campos (preservando usuario y fechas)
            existing.setNombreCompleto(entrenadorActualizado.getNombreCompleto());
            existing.setCedula(entrenadorActualizado.getCedula());
            existing.setTelefono(entrenadorActualizado.getTelefono());
            existing.setEmail(entrenadorActualizado.getEmail());
            existing.setEquipo(entrenadorActualizado.getEquipo());
            existing.setCategoria(entrenadorActualizado.getCategoria());
            existing.setExperiencia(entrenadorActualizado.getExperiencia());
            existing.setEspecialidades(entrenadorActualizado.getEspecialidades());
            existing.setFechaActualizacion(LocalDateTime.now());

            // Actualizar email del usuario si cambió
            if (existing.getUsuario() != null && 
                !existing.getUsuario().getEmail().equals(entrenadorActualizado.getEmail())) {
                existing.getUsuario().setEmail(entrenadorActualizado.getEmail());
                usuarioRepository.save(existing.getUsuario());
            }

            return entrenadorRepository.save(existing);

        } catch (Exception e) {
            throw new RuntimeException("Error al actualizar entrenador: " + e.getMessage());
        }
    }

    /**
     * Métodos para reseñas
     */
public List<Reseña> findReseñasByEntrenador(Long entrenadorId) {
    return reseñaRepository.findByEntrenador_IdOrderByFechaCreacionDesc(entrenadorId);
}

public double getPromedioCalificaciones(Long entrenadorId) {
    Double promedio = reseñaRepository.findPromedioCalificacionByEntrenador(entrenadorId);
    return promedio != null ? promedio : 0.0;
}

    /**
     * Métodos adicionales para estadísticas
     */
    public List<Entrenador> findTop5ByCategoria(Entrenador.Categoria categoria) {
        return entrenadorRepository.findTop5ByCategoriaAndActivoTrueOrderByFechaCreacionDesc(categoria);
    }

    public List<Entrenador> findEntrenadoresConMasExperiencia() {
        return entrenadorRepository.findByActivoTrueOrderByExperienciaDesc();
    }

    public List<Entrenador> findRecentlyCreated() {
        LocalDateTime unMesAtras = LocalDateTime.now().minusMonths(1);
        return entrenadorRepository.findByActivoTrueAndFechaCreacionAfterOrderByFechaCreacionDesc(unMesAtras);
    }

    /**
     * Cambiar estado de activación
     */
    public void activateCoach(Long id) {
        Optional<Entrenador> entrenadorOpt = entrenadorRepository.findById(id);
        if (entrenadorOpt.isPresent()) {
            Entrenador entrenador = entrenadorOpt.get();
            entrenador.setActivo(true);
            entrenador.setFechaActualizacion(LocalDateTime.now());
            entrenadorRepository.save(entrenador);
        }
    }

    public void deactivateCoach(Long id) {
        Optional<Entrenador> entrenadorOpt = entrenadorRepository.findById(id);
        if (entrenadorOpt.isPresent()) {
            Entrenador entrenador = entrenadorOpt.get();
            entrenador.setActivo(false);
            entrenador.setFechaActualizacion(LocalDateTime.now());
            entrenadorRepository.save(entrenador);
        }
    }

    /**
     * Resetear contraseña de un coach
     */
    public void resetPassword(Long entrenadorId) {
        Optional<Entrenador> entrenadorOpt = entrenadorRepository.findById(entrenadorId);
        if (entrenadorOpt.isPresent()) {
            Entrenador entrenador = entrenadorOpt.get();
            if (entrenador.getUsuario() != null) {
                Usuario usuario = entrenador.getUsuario();
                usuario.setPassword(entrenador.getCedula()); // Reset a la cédula
                usuarioRepository.save(usuario);
            }
        }
    }

    /**
     * Buscar entrenadores por múltiples criterios
     */
    public List<Entrenador> findByCriteria(String nombre, String equipo, Entrenador.Categoria categoria) {
        if (nombre != null && !nombre.trim().isEmpty()) {
            return findByNombreCompleto(nombre);
        } else if (equipo != null && !equipo.trim().isEmpty()) {
            return findByEquipo(equipo);
        } else if (categoria != null) {
            return findByCategoria(categoria);
        } else {
            return findAllActive();
        }
    }
}