package eafit.caba_pro.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import eafit.caba_pro.model.Arbitro;
import eafit.caba_pro.model.Partido;
import jakarta.transaction.Transactional;

@Repository
public interface PartidoRepository extends JpaRepository<Partido, Long> {

    List<Partido> findByTorneoIsNull();           
    boolean existsByArbitroId(Long arbitroId);
    List<Partido> findByTorneo_Id(Long torneoId);
    List<Partido> findByTorneoIsNullAndEstado(Partido.EstadoPartido estado);
    boolean existsByTorneo_Id(Long torneoId);
    long countByTorneo_Id(Long torneoId);

    
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

    @Query("SELECT COUNT(p) FROM Partido p WHERE p.estado = :estado")
    long countByEstado(@Param("estado") Partido.EstadoPartido estado);

    List<Partido> findByArbitroAndFechaBetweenAndLiquidacionIsNull(Arbitro arbitro,LocalDate fechaInicio,LocalDate fechaFin);
    
    @Transactional
    @Modifying
    @Query("update Partido p set p.torneo = null where p.torneo.id = :torneoId")
    int unassignByTorneoId(Long torneoId);
    
    
// AGREGAR ESTOS MÉTODOS A TU PartidoRepository EXISTENTE

    // ==================== MÉTODOS PARA ESTADÍSTICAS DE EQUIPOS ====================
    
    /**
     * Contar partidos de un equipo (como local o visitante)
     */
    @Query("SELECT COUNT(p) FROM Partido p WHERE p.equipoLocal.nombre = :equipo OR p.equipoVisitante.nombre = :equipo")
    int countPartidosByEquipo(@Param("equipo") String equipo);

    /**
     * Contar partidos ganados por un equipo
     */
    @Query("SELECT COUNT(p) FROM Partido p WHERE " +
           "(p.equipoLocal.nombre = :equipo AND p.golesLocal > p.golesVisitante AND p.estado = 'FINALIZADO') OR " +
           "(p.equipoVisitante.nombre = :equipo AND p.golesVisitante > p.golesLocal AND p.estado = 'FINALIZADO')")
    int countPartidosGanadosByEquipo(@Param("equipo") String equipo);

    /**
     * Contar partidos perdidos por un equipo
     */
    @Query("SELECT COUNT(p) FROM Partido p WHERE " +
           "(p.equipoLocal.nombre = :equipo AND p.golesLocal < p.golesVisitante AND p.estado = 'FINALIZADO') OR " +
           "(p.equipoVisitante.nombre = :equipo AND p.golesVisitante < p.golesLocal AND p.estado = 'FINALIZADO')")
    int countPartidosPerdidosByEquipo(@Param("equipo") String equipo);

    /**
     * Contar partidos empatados por un equipo
     */
    @Query("SELECT COUNT(p) FROM Partido p WHERE " +
           "(p.equipoLocal.nombre = :equipo OR p.equipoVisitante.nombre = :equipo) AND " +
           "p.golesLocal = p.golesVisitante AND p.estado = 'FINALIZADO'")
    int countPartidosEmpatadosByEquipo(@Param("equipo") String equipo);

    /**
     * Obtener partidos programados de un equipo
     */
    @Query("SELECT p FROM Partido p WHERE " +
           "(p.equipoLocal.nombre = :equipo OR p.equipoVisitante.nombre = :equipo) AND " +
           "p.estado = 'PROGRAMADO' ORDER BY p.fecha ASC, p.hora ASC")
    List<Partido> findPartidosProgramadosByEquipo(@Param("equipo") String equipo);

    /**
     * Obtener partidos finalizados de un equipo
     */
    @Query("SELECT p FROM Partido p WHERE " +
           "(p.equipoLocal.nombre = :equipo OR p.equipoVisitante.nombre = :equipo) AND " +
           "p.estado = 'FINALIZADO' ORDER BY p.fecha DESC, p.hora DESC")
    List<Partido> findPartidosFinalizadosByEquipo(@Param("equipo") String equipo);

    /**
     * Obtener últimos 5 partidos finalizados de un equipo
     */
    @Query("SELECT p FROM Partido p WHERE " +
           "(p.equipoLocal.nombre = :equipo OR p.equipoVisitante.nombre = :equipo) AND " +
           "p.estado = 'FINALIZADO' AND p.fecha <= CURRENT_DATE " +
           "ORDER BY p.fecha DESC, p.hora DESC LIMIT 5")
    List<Partido> findUltimos5PartidosByEquipo(@Param("equipo") String equipo);

    /**
     * Obtener próximos 5 partidos programados de un equipo
     */
    @Query("SELECT p FROM Partido p WHERE " +
           "(p.equipoLocal.nombre = :equipo OR p.equipoVisitante.nombre = :equipo) AND " +
           "p.estado = 'PROGRAMADO' AND p.fecha >= CURRENT_DATE " +
           "ORDER BY p.fecha ASC, p.hora ASC LIMIT 5")
    List<Partido> findProximos5PartidosByEquipo(@Param("equipo") String equipo);
    
    // Encontrar partidos finalizados por árbitro
    @Query("SELECT p FROM Partido p WHERE p.arbitro = :arbitro AND p.estado = :estado ORDER BY p.fecha DESC")
    List<Partido> findByArbitroAndEstado(@Param("arbitro") Arbitro arbitro, @Param("estado") Partido.EstadoPartido estado);
    
    // Encontrar partidos finalizados por árbitro y que involucren un equipo específico
    @Query("SELECT p FROM Partido p WHERE p.arbitro = :arbitro AND p.estado = :estado " +
           "AND (p.equipoLocal.nombre = :equipoNombre OR p.equipoVisitante.nombre = :equipoNombre) " +
           "ORDER BY p.fecha DESC")
    List<Partido> findByArbitroAndEstadoAndEquipo(@Param("arbitro") Arbitro arbitro, 
                                                  @Param("estado") Partido.EstadoPartido estado,
                                                  @Param("equipoNombre") String equipoNombre);
}