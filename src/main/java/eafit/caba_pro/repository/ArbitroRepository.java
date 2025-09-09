package eafit.caba_pro.repository;

import eafit.caba_pro.model.Arbitro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ArbitroRepository extends JpaRepository<Arbitro, Long> {
    
    
    // Búsqueda por cédula (para validar duplicados)
    Optional<Arbitro> findByCedula(String cedula);

    // Búsqueda por nombre de usuario (para login)
    Optional<Arbitro> findByUsername(String username);

    // Verificación de existencia (para validaciones)
    boolean existsByCedula(String cedula);
    boolean existsByPhone(String phone);
    
    // Ordenar por nombre (usado en findAllOrderByNombre)
    List<Arbitro> findAllByOrderByNombreAsc();
    
    // Búsqueda por escala
    List<Arbitro> findByScale(String scale);
    List<Arbitro> findByScaleOrderByNombreAsc(String scale);
    
    // Búsqueda por nombre (case-insensitive)
    List<Arbitro> findByNombreContainingIgnoreCase(String nombre);
    
    // Búsqueda combinada
    List<Arbitro> findByScaleAndNombreContainingIgnoreCase(String scale, String nombre);
    
    // MÉTODOS PARA LA RELACIÓN CON PARTIDOS
    
    // Encontrar árbitros disponibles en una fecha y hora específica
    @Query("SELECT a FROM Arbitro a WHERE a.id NOT IN " +
           "(SELECT p.arbitro.id FROM Partido p WHERE p.arbitro IS NOT NULL " +
           "AND p.fecha = :fecha AND p.hora = :hora)")
    List<Arbitro> findAvailableArbitrosAt(@Param("fecha") LocalDate fecha, 
                                         @Param("hora") LocalTime hora);
    
    // Encontrar árbitros con partidos en un rango de fechas
    @Query("SELECT DISTINCT a FROM Arbitro a JOIN a.partidos p " +
           "WHERE p.fecha BETWEEN :fechaInicio AND :fechaFin")
    List<Arbitro> findArbitrosWithPartidosBetween(@Param("fechaInicio") LocalDate fechaInicio, 
                                                  @Param("fechaFin") LocalDate fechaFin);
    
    // Encontrar árbitros sin partidos asignados
    @Query("SELECT a FROM Arbitro a WHERE a.partidos IS EMPTY")
    List<Arbitro> findArbitrosSinPartidos();
    
    // Contar partidos de un árbitro
    @Query("SELECT COUNT(p) FROM Arbitro a JOIN a.partidos p WHERE a.id = :arbitroId")
    Long countPartidosByArbitroId(@Param("arbitroId") Long arbitroId);

    Arbitro save(Arbitro arbitr);
    
}