package eafit.caba_pro.repository;

import eafit.caba_pro.model.Reseña;
import eafit.caba_pro.model.Arbitro;
import eafit.caba_pro.model.Partido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReseñaRepository extends JpaRepository<Reseña, Long> {
    
    // Encontrar todas las reseñas de un árbitro específico ordenadas por fecha
    List<Reseña> findByArbitroOrderByFechaCreacionDesc(Arbitro arbitro);
    
    // Obtener el promedio de puntuación de un árbitro
    @Query("SELECT AVG(r.puntuacion) FROM Reseña r WHERE r.arbitro = :arbitro")
    Double findAverageRatingByArbitro(@Param("arbitro") Arbitro arbitro);
    
    // Contar el número de reseñas de un árbitro
    long countByArbitro(Arbitro arbitro);
    
    // Encontrar reseñas por puntuación específica
    List<Reseña> findByArbitroAndPuntuacion(Arbitro arbitro, Integer puntuacion);
    
    // Encontrar las mejores reseñas (4 y 5 estrellas)
    @Query("SELECT r FROM Reseña r WHERE r.arbitro = :arbitro AND r.puntuacion >= 4 ORDER BY r.fechaCreacion DESC")
    List<Reseña> findTopReseñasByArbitro(@Param("arbitro") Arbitro arbitro);
    
    // Encontrar reseñas por partido específico
    List<Reseña> findByPartidoOrderByFechaCreacionDesc(Partido partido);
    
    // Encontrar reseñas de un árbitro en un partido específico
    List<Reseña> findByArbitroAndPartido(Arbitro arbitro, Partido partido);
    
    // Verificar si ya existe una reseña de un entrenador para un partido específico
    @Query("SELECT r FROM Reseña r WHERE r.arbitro = :arbitro AND r.partido = :partido AND r.entrenador.id = :entrenadorId")
    List<Reseña> findByArbitroAndPartidoAndEntrenadorId(@Param("arbitro") Arbitro arbitro, 
                                                        @Param("partido") Partido partido, 
                                                        @Param("entrenadorId") Long entrenadorId);
}
