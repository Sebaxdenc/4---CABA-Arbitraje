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
    
    private PartidoRepository partidoRepository;    
    private ArbitroRepository arbitroRepository;
    private final NotificacionService notificacionService;

    public PartidoService(PartidoRepository partidoRepository, 
                         ArbitroRepository arbitroRepository,
                         NotificacionService notificacionService) {
        this.partidoRepository = partidoRepository;
        this.arbitroRepository = arbitroRepository;
        this.notificacionService = notificacionService;
    }
    
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
    
    public List<Partido> findByArbitro(Arbitro arbitro) {
        return partidoRepository.findByArbitro(arbitro);
    }
    
    public List<Partido> findFuturePartidosByArbitro(Arbitro arbitro) {
        return partidoRepository.findFuturePartidosByArbitro(arbitro, LocalDate.now());
    }
    
    public List<Partido> findPastPartidosByArbitro(Arbitro arbitro) {
        return partidoRepository.findPastPartidosByArbitro(arbitro, LocalDate.now());
    }
    
    // Métodos para obtener partidos ordenados
    public List<Partido> findPartidosPasadosOrdenados(Arbitro arbitro) {
        List<Partido> todosLosPartidos = partidoRepository.findByArbitro(arbitro);
        LocalDate hoy = LocalDate.now();
        
        return todosLosPartidos.stream()
            .filter(partido -> partido.getFecha().isBefore(hoy))
            .sorted(java.util.Comparator.comparing(Partido::getFecha).reversed()) // Más recientes primero
            .collect(java.util.stream.Collectors.toList());
    }
    
    public List<Partido> findPartidosFuturosOrdenados(Arbitro arbitro) {
        List<Partido> todosLosPartidos = partidoRepository.findByArbitro(arbitro);
        LocalDate hoy = LocalDate.now();
        
        return todosLosPartidos.stream()
            .filter(partido -> partido.getFecha().isAfter(hoy) || partido.getFecha().isEqual(hoy))
            .sorted(java.util.Comparator.comparing(Partido::getFecha)) // Más próximos primero
            .collect(java.util.stream.Collectors.toList());
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
        // Una sola consulta que obtiene todos los partidos
        List<Partido> todosPartidos = findPartidosByArbitro(arbitro);
        LocalDate hoy = LocalDate.now();
        
        // Separar en memoria en lugar de hacer más consultas a BD
        int partidosFuturos = 0;
        int partidosPasados = 0;
        
        for (Partido partido : todosPartidos) {
            if (partido.getFecha().isAfter(hoy) || 
                (partido.getFecha().isEqual(hoy) && partido.getHora().isAfter(java.time.LocalTime.now()))) {
                partidosFuturos++;
            } else {
                partidosPasados++;
            }
        }
        
        Map<String, Object> estadisticas = new HashMap<>();
        estadisticas.put("totalPartidos", todosPartidos.size());
        estadisticas.put("partidosFuturos", partidosFuturos);
        estadisticas.put("partidosPasados", partidosPasados);
        
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
        
        // Guardar el partido primero
        Partido partidoGuardado = save(partido);
        
        // Si hay un árbitro asignado, notificar que tiene un partido pendiente por confirmar
        if (partidoGuardado.getArbitro() != null) {
            String mensaje = "Nuevo partido asignado: Tiene un partido pendiente por confirmar" +
                            " programado para el " + partidoGuardado.getFecha() + " a las " + partidoGuardado.getHora() +
                            ". Por favor confirme su disponibilidad.";

            notificacionService.notificarArbitro(mensaje, partidoGuardado.getArbitro());
        }

        return partidoGuardado;
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

    public long count() {
    return partidoRepository.count();
    }

    public long countByEstado(Partido.EstadoPartido estado) {
        return partidoRepository.countByEstado(estado);
    }
    // Porcentaje partidos aceptados/rechazados
    public Map<String, Double> getPorcentajeAceptadosRechazados() {
        long total = partidoRepository.count();
        long aceptados = partidoRepository.countByEstado(Partido.EstadoPartido.PROGRAMADO);

        Map<String, Double> porcentajes = new HashMap<>();
        porcentajes.put("aceptados", total > 0 ? (aceptados * 100.0 / total) : 0);
        return porcentajes;
    }

    // ========== MÉTODOS ADICIONALES PARA DISPONIBILIDAD ==========

    /**
     * Buscar partidos por árbitro y estado
     */
    public List<Partido> findByArbitroAndEstado(Arbitro arbitro, Partido.EstadoPartido estado) {
        return partidoRepository.findAll().stream()
                .filter(partido -> partido.getArbitro() != null && 
                                 partido.getArbitro().getId().equals(arbitro.getId()) && 
                                 partido.getEstado() == estado)
                .toList();
    }

// ==================== MÉTODOS PARA ENTRENADORES/COACHES ====================

/**
 * Contar partidos por equipo (como local o visitante)
 */
public int countPartidosByEquipo(String equipo) {
    return partidoRepository.countPartidosByEquipo(equipo);
}

/**
 * Contar partidos ganados por un equipo
 */
public int countPartidosGanadosByEquipo(String equipo) {
    return partidoRepository.countPartidosGanadosByEquipo(equipo);
}

/**
 * Contar partidos perdidos por un equipo
 */
public int countPartidosPerdidosByEquipo(String equipo) {
    return partidoRepository.countPartidosPerdidosByEquipo(equipo);
}

/**
 * Contar partidos empatados por un equipo
 */
public int countPartidosEmpatadosByEquipo(String equipo) {
    return partidoRepository.countPartidosEmpatadosByEquipo(equipo);
}

/**
 * Obtener partidos programados de un equipo
 */
public List<Partido> findPartidosProgramadosByEquipo(String equipo) {
    return partidoRepository.findPartidosProgramadosByEquipo(equipo);
}

/**
 * Obtener partidos finalizados de un equipo
 */
public List<Partido> findPartidosFinalizadosByEquipo(String equipo) {
    return partidoRepository.findPartidosFinalizadosByEquipo(equipo);
}

/**
 * Obtener últimos 5 partidos de un equipo
 */
public List<Partido> findUltimos5PartidosByEquipo(String equipo) {
    return partidoRepository.findUltimos5PartidosByEquipo(equipo);
}

/**
 * Obtener próximos 5 partidos de un equipo
 */
public List<Partido> findProximos5PartidosByEquipo(String equipo) {
    return partidoRepository.findProximos5PartidosByEquipo(equipo);
}

/**
 * Obtener estadísticas detalladas de un equipo
 */
public Map<String, Object> getEstadisticasDetalladasByEquipo(String equipo) {
    Map<String, Object> estadisticas = new HashMap<>();
    
    int totalPartidos = countPartidosByEquipo(equipo);
    int ganados = countPartidosGanadosByEquipo(equipo);
    int perdidos = countPartidosPerdidosByEquipo(equipo);
    int empatados = countPartidosEmpatadosByEquipo(equipo);
    
    double porcentajeVictorias = totalPartidos > 0 ? (ganados * 100.0 / totalPartidos) : 0;
    double porcentajeDerrotas = totalPartidos > 0 ? (perdidos * 100.0 / totalPartidos) : 0;
    double porcentajeEmpates = totalPartidos > 0 ? (empatados * 100.0 / totalPartidos) : 0;
    
    estadisticas.put("totalPartidos", totalPartidos);
    estadisticas.put("partidosGanados", ganados);
    estadisticas.put("partidosPerdidos", perdidos);
    estadisticas.put("partidosEmpatados", empatados);
    estadisticas.put("porcentajeVictorias", porcentajeVictorias);
    estadisticas.put("porcentajeDerrotas", porcentajeDerrotas);
    estadisticas.put("porcentajeEmpates", porcentajeEmpates);
    
    return estadisticas;
}

/**
 * Obtener partidos finalizados de un árbitro específico
 */
public List<Partido> findPartidosFinalizadosByArbitro(Long arbitroId) {
    Optional<Arbitro> arbitroOpt = arbitroRepository.findById(arbitroId);
    if (arbitroOpt.isEmpty()) {
        return new ArrayList<>();
    }
    
    Arbitro arbitro = arbitroOpt.get();
    return partidoRepository.findByArbitroAndEstado(arbitro, Partido.EstadoPartido.FINALIZADO);
}

/**
 * Obtener partidos finalizados por árbitro y equipo específico
 */
public List<Partido> findPartidosFinalizadosByArbitroYEquipo(Long arbitroId, String equipoNombre) {
    Optional<Arbitro> arbitroOpt = arbitroRepository.findById(arbitroId);
    if (arbitroOpt.isEmpty()) {
        return new ArrayList<>();
    }
    
    Arbitro arbitro = arbitroOpt.get();
    return partidoRepository.findByArbitroAndEstadoAndEquipo(arbitro, Partido.EstadoPartido.FINALIZADO, equipoNombre);
}
}