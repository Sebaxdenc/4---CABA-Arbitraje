package eafit.caba_pro.service;

import eafit.caba_pro.model.Reseña;
import eafit.caba_pro.model.Arbitro;
import eafit.caba_pro.repository.ReseñaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ReseñaService {
    
    @Autowired
    private ReseñaRepository reseñaRepository;
    
    // CRUD básico
    public List<Reseña> findAll() {
        return reseñaRepository.findAll();
    }
    
    public Optional<Reseña> findById(Long id) {
        return reseñaRepository.findById(id);
    }
    
    public Reseña save(Reseña reseña) {
        return reseñaRepository.save(reseña);
    }
    
    public boolean deleteById(Long id) {
        if (reseñaRepository.existsById(id)) {
            reseñaRepository.deleteById(id);
            return true;
        }
        return false;
    }
    
    // Métodos específicos para árbitros
    public List<Reseña> findReseñasByArbitro(Arbitro arbitro) {
        return reseñaRepository.findByArbitroOrderByFechaCreacionDesc(arbitro);
    }
    
    public Double getPromedioReseñas(Arbitro arbitro) {
        Double promedio = reseñaRepository.findAverageRatingByArbitro(arbitro);
        return promedio != null ? promedio : 0.0;
    }
    
    public long contarReseñas(Arbitro arbitro) {
        return reseñaRepository.countByArbitro(arbitro);
    }
    
    public List<Reseña> getMejoresReseñas(Arbitro arbitro) {
        return reseñaRepository.findTopReseñasByArbitro(arbitro);
    }
    
    // Método para obtener estadísticas de reseñas
    public ReseñaStats getEstadisticasReseñas(Arbitro arbitro) {
        List<Reseña> todasLasReseñas = findReseñasByArbitro(arbitro);
        Double promedio = getPromedioReseñas(arbitro);
        long total = contarReseñas(arbitro);
        
        // Contar por puntuación
        long estrella5 = todasLasReseñas.stream().filter(r -> r.getPuntuacion() == 5).count();
        long estrella4 = todasLasReseñas.stream().filter(r -> r.getPuntuacion() == 4).count();
        long estrella3 = todasLasReseñas.stream().filter(r -> r.getPuntuacion() == 3).count();
        long estrella2 = todasLasReseñas.stream().filter(r -> r.getPuntuacion() == 2).count();
        long estrella1 = todasLasReseñas.stream().filter(r -> r.getPuntuacion() == 1).count();
        
        return new ReseñaStats(promedio, total, estrella5, estrella4, estrella3, estrella2, estrella1);
    }
    
    // Clase interna para estadísticas
    public static class ReseñaStats {
        private final Double promedio;
        private final long total;
        private final long estrella5;
        private final long estrella4;
        private final long estrella3;
        private final long estrella2;
        private final long estrella1;
        
        public ReseñaStats(Double promedio, long total, long estrella5, long estrella4, 
                          long estrella3, long estrella2, long estrella1) {
            this.promedio = promedio;
            this.total = total;
            this.estrella5 = estrella5;
            this.estrella4 = estrella4;
            this.estrella3 = estrella3;
            this.estrella2 = estrella2;
            this.estrella1 = estrella1;
        }
        
        // Getters
        public Double getPromedio() { return promedio; }
        public long getTotal() { return total; }
        public long getEstrella5() { return estrella5; }
        public long getEstrella4() { return estrella4; }
        public long getEstrella3() { return estrella3; }
        public long getEstrella2() { return estrella2; }
        public long getEstrella1() { return estrella1; }
        
        public String getPromedioFormateado() {
            return String.format("%.1f", promedio);
        }
        
        public String getPromedioBalones() {
            int balonesCompletos = (int) Math.floor(promedio);
            StringBuilder balones = new StringBuilder();
            
            for (int i = 1; i <= 5; i++) {
                if (i <= balonesCompletos) {
                    balones.append("🏀");
                } else if (i == balonesCompletos + 1 && promedio % 1 >= 0.5) {
                    balones.append("🟠"); // Medio balón
                } else {
                    balones.append("⚪");
                }
            }
            return balones.toString();
        }
    }
}
