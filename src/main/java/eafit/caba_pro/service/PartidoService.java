package eafit.caba_pro.service;

import eafit.caba_pro.model.Partido;
import eafit.caba_pro.model.Arbitro;
import eafit.caba_pro.repository.PartidoRepository;
import eafit.caba_pro.repository.ArbitroRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

@Service
public class PartidoService {
    
    @Autowired
    private PartidoRepository partidoRepository;
    
    @Autowired
    private ArbitroRepository arbitroRepository;
    
    // CRUD básico
    public List<Partido> findAll() {
        return partidoRepository.findAll();
    }
    
    public Optional<Partido> findById(Long id) {
        return partidoRepository.findById(id);
    }
    
    public Partido save(Partido partido) {
        return partidoRepository.save(partido);
    }
    
    public boolean deleteById(Long id) {
        if (partidoRepository.existsById(id)) {
            partidoRepository.deleteById(id);
            return true;
        }
        return false;
    }
    
    // Métodos específicos para árbitros
    public List<Partido> findPartidosByArbitro(Arbitro arbitro) {
        return partidoRepository.findByArbitro(arbitro);
    }
    
    public List<Partido> findFuturePartidosByArbitro(Arbitro arbitro) {
        return partidoRepository.findFuturePartidosByArbitro(arbitro, LocalDate.now());
    }
    
    public List<Partido> findPastPartidosByArbitro(Arbitro arbitro) {
        return partidoRepository.findPastPartidosByArbitro(arbitro, LocalDate.now());
    }
    
    // Método para obtener datos del calendario de un árbitro
    public Map<String, Object> getCalendarioDataByArbitro(Arbitro arbitro, YearMonth yearMonth) {
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        List<Partido> partidos = partidoRepository.findByArbitroAndFechaBetween(
            arbitro, startDate, endDate);

        Map<String, Object> calendarioData = new HashMap<>();
        Map<String, List<Map<String, Object>>> partidosPorFecha = new HashMap<>();
        
        for (Partido partido : partidos) {
            String fechaStr = partido.getFecha().toString();
            
            Map<String, Object> partidoData = new HashMap<>();
            partidoData.put("id", partido.getId());
            partidoData.put("equipoLocal", partido.getEquipoLocal().getNombre());
            partidoData.put("equipoVisitante", partido.getEquipoVisitante().getNombre());
            partidoData.put("hora", partido.getHora().toString());
            partidoData.put("estado", partido.getEstado().toString());
            partidoData.put("esFuturo", partido.esPartidoFuturo());
            
            partidosPorFecha.computeIfAbsent(fechaStr, k -> new ArrayList<>()).add(partidoData);
        }
        
        calendarioData.put("partidos", partidosPorFecha);
        calendarioData.put("year", yearMonth.getYear());
        calendarioData.put("month", yearMonth.getMonthValue());
        calendarioData.put("totalPartidos", partidos.size());
        
        return calendarioData;
    }
    
    // Verificar si un árbitro está disponible
    public boolean isArbitroDisponible(Arbitro arbitro, LocalDate fecha, java.time.LocalTime hora) {
        return !partidoRepository.isArbitroOcupado(arbitro, fecha, hora);
    }
    
    // Obtener estadísticas de un árbitro
    public Map<String, Object> getEstadisticasArbitro(Arbitro arbitro) {
        List<Partido> todosPartidos = findPartidosByArbitro(arbitro);
        List<Partido> partidosFuturos = findFuturePartidosByArbitro(arbitro);
        List<Partido> partidosPasados = findPastPartidosByArbitro(arbitro);
        
        Map<String, Object> estadisticas = new HashMap<>();
        estadisticas.put("totalPartidos", todosPartidos.size());
        estadisticas.put("partidosFuturos", partidosFuturos.size());
        estadisticas.put("partidosPasados", partidosPasados.size());
        
        // Como ahora solo hay un árbitro por partido, todos son como principal
        estadisticas.put("comoArbitroPrincipal", todosPartidos.size());
        estadisticas.put("comoArbitroAuxiliar", 0);
        
        return estadisticas;
    }
    
    // Validar partido antes de guardar
    public void validarPartido(Partido partido) {
        if (partido.getFecha().isBefore(LocalDate.now())) {
            throw new RuntimeException("No se puede crear un partido en una fecha pasada");
        }
        
        if (partido.getArbitro() != null && 
            !isArbitroDisponible(partido.getArbitro(), partido.getFecha(), partido.getHora())) {
            throw new RuntimeException("El árbitro no está disponible en esa fecha y hora");
        }
    }
    
    public Partido crearPartido(Partido partido) {
        validarPartido(partido);
        return save(partido);
    }
    
    // MÉTODOS NUEVOS PARA LA RELACIÓN BIDIRECCIONAL
    
    /**
     * Asignar árbitro a un partido usando la relación bidireccional
     */
    @Transactional
    public boolean asignarArbitroAPartido(Long partidoId, Long arbitroId) {
        Optional<Partido> partidoOpt = partidoRepository.findById(partidoId);
        Optional<Arbitro> arbitroOpt = arbitroRepository.findById(arbitroId);
        
        if (partidoOpt.isPresent() && arbitroOpt.isPresent()) {
            Partido partido = partidoOpt.get();
            Arbitro arbitro = arbitroOpt.get();
            
            // Verificar disponibilidad
            if (!isArbitroDisponible(arbitro, partido.getFecha(), partido.getHora())) {
                throw new RuntimeException("El árbitro no está disponible en esa fecha y hora");
            }
            
            // Usar el método helper del árbitro para mantener sincronización
            arbitro.addPartido(partido);
            arbitroRepository.save(arbitro);
            return true;
        }
        return false;
    }
    
    /**
     * Desasignar árbitro de un partido
     */
    @Transactional
    public boolean desasignarArbitroDePartido(Long partidoId) {
        Optional<Partido> partidoOpt = partidoRepository.findById(partidoId);
        if (partidoOpt.isPresent()) {
            Partido partido = partidoOpt.get();
            if (partido.getArbitro() != null) {
                Arbitro arbitro = partido.getArbitro();
                arbitro.removePartido(partido); // Usa el método helper
                arbitroRepository.save(arbitro);
                return true;
            }
        }
        return false;
    }
    
    /**
     * Obtener partidos sin árbitro asignado
     */
    public List<Partido> getPartidosSinArbitro() {
        return partidoRepository.findPartidosSinArbitro();
    }
    
    /**
     * Obtener partidos sin árbitro en un rango de fechas
     */
    public List<Partido> getPartidosSinArbitro(LocalDate fechaInicio, LocalDate fechaFin) {
        return partidoRepository.findPartidosSinArbitroBetween(fechaInicio, fechaFin);
    }
    
    /**
     * Contar partidos de un árbitro
     */
    public Long contarPartidosDeArbitro(Arbitro arbitro) {
        return partidoRepository.countByArbitro(arbitro);
    }
}
