package eafit.caba_pro.repository;

import eafit.caba_pro.model.Partido;
import eafit.caba_pro.model.Arbitro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface PartidoRepository extends JpaRepository<Partido, Long> {
    
    // Encontrar partidos por árbitro
    @Query("SELECT p FROM Partido p WHERE p.arbitro = :arbitro")
    List<Partido> findByArbitro(@Param("arbitro") Arbitro arbitro);
    
    // Encontrar partidos de un árbitro en un rango de fechas
    @Query("SELECT p FROM Partido p WHERE p.arbitro = :arbitro " +
           "AND p.fecha BETWEEN :fechaInicio AND :fechaFin")
    List<Partido> findByArbitroAndFechaBetween(@Param("arbitro") Arbitro arbitro, 
                                               @Param("fechaInicio") LocalDate fechaInicio, 
                                               @Param("fechaFin") LocalDate fechaFin);
    
    // Encontrar partidos futuros de un árbitro
    @Query("SELECT p FROM Partido p WHERE p.arbitro = :arbitro " +
           "AND p.fecha >= :fecha ORDER BY p.fecha ASC, p.hora ASC")
    List<Partido> findFuturePartidosByArbitro(@Param("arbitro") Arbitro arbitro, @Param("fecha") LocalDate fecha);
    
    // Encontrar partidos pasados de un árbitro
    @Query("SELECT p FROM Partido p WHERE p.arbitro = :arbitro " +
           "AND p.fecha < :fecha ORDER BY p.fecha DESC, p.hora DESC")
    List<Partido> findPastPartidosByArbitro(@Param("arbitro") Arbitro arbitro, @Param("fecha") LocalDate fecha);
    
    // Encontrar partidos por fecha específica
    List<Partido> findByFecha(LocalDate fecha);
    
    // Encontrar partidos en un rango de fechas
    List<Partido> findByFechaBetween(LocalDate fechaInicio, LocalDate fechaFin);
    
    // Encontrar partidos por estado
    List<Partido> findByEstado(Partido.EstadoPartido estado);
    
    // Verificar disponibilidad de un árbitro en una fecha/hora específica
    @Query("SELECT COUNT(p) > 0 FROM Partido p WHERE p.arbitro = :arbitro " +
           "AND p.fecha = :fecha AND p.hora = :hora")
    boolean isArbitroOcupado(@Param("arbitro") Arbitro arbitro, 
                             @Param("fecha") LocalDate fecha, 
                             @Param("hora") java.time.LocalTime hora);
    
    // Obtener fechas con partidos para un árbitro (para el calendario)
    @Query("SELECT DISTINCT p.fecha FROM Partido p WHERE p.arbitro = :arbitro")
    List<LocalDate> findDistinctFechasByArbitro(@Param("arbitro") Arbitro arbitro);
    
    // MÉTODOS ADICIONALES PARA LA RELACIÓN BIDIRECCIONAL
    
    // Encontrar partidos sin árbitro asignado
    @Query("SELECT p FROM Partido p WHERE p.arbitro IS NULL")
    List<Partido> findPartidosSinArbitro();
    
    // Encontrar partidos sin árbitro en un rango de fechas
    @Query("SELECT p FROM Partido p WHERE p.arbitro IS NULL " +
           "AND p.fecha BETWEEN :fechaInicio AND :fechaFin")
    List<Partido> findPartidosSinArbitroBetween(@Param("fechaInicio") LocalDate fechaInicio, 
                                                @Param("fechaFin") LocalDate fechaFin);
    
    // Contar partidos por árbitro
    @Query("SELECT COUNT(p) FROM Partido p WHERE p.arbitro = :arbitro")
    Long countByArbitro(@Param("arbitro") Arbitro arbitro);
    
}