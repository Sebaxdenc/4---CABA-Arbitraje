package eafit.caba_pro.repository;

import eafit.caba_pro.model.Entrenador; 
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EntrenadorRepository extends JpaRepository<Entrenador, Long> {
    
    // Método básico para encontrar entrenadores activos
    List<Entrenador> findByActivoTrue();
    
    // Buscar por documento
    Optional<Entrenador> findByDocumento(String documento);
    
    // Buscar por email
    Optional<Entrenador> findByEmail(String email);
    
    // Buscar por equipo
    List<Entrenador> findByEquipoContainingIgnoreCase(String equipo);
    
    // Buscar entrenadores por categoría
    List<Entrenador> findByCategoria(Entrenador.Categoria categoria);
    
    // Buscar entrenadores activos por equipo
    List<Entrenador> findByEquipoAndActivoTrue(String equipo);
    
    // Query personalizada para buscar entrenadores con años de experiencia mayor a X
    @Query("SELECT e FROM Entrenador e WHERE e.anosExperiencia >= :anos AND e.activo = true")
    List<Entrenador> findEntrenadoresWithExperience(@Param("anos") Integer anos);
    
    // Query para buscar por nombre completo
    @Query("SELECT e FROM Entrenador e WHERE LOWER(CONCAT(e.nombre, ' ', e.apellidos)) LIKE LOWER(CONCAT('%', :nombreCompleto, '%'))")
    List<Entrenador> findByNombreCompletoContaining(@Param("nombreCompleto") String nombreCompleto);
    
    // Contar entrenadores activos
    long countByActivoTrue();
    
    // Contar entrenadores por equipo
    long countByEquipo(String equipo);
}