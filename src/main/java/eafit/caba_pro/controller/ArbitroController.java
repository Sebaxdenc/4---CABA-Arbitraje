package eafit.caba_pro.controller;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import eafit.caba_pro.model.Arbitro;
import eafit.caba_pro.model.Partido;
import eafit.caba_pro.service.ArbitroService;
import eafit.caba_pro.service.PartidoService;
import eafit.caba_pro.service.UsuarioService;
import eafit.caba_pro.service.ReseñaService;


@Controller
@RequestMapping("/arbitro")
public class ArbitroController {

    private final ArbitroService arbitroService;
    private final PartidoService partidoService;
    private final UsuarioService usuarioService;
    private final ReseñaService reseñaService;

    public ArbitroController(ArbitroService arbitroService, PartidoService partidoService, UsuarioService usuarioService, ReseñaService reseñaService) {
        this.arbitroService = arbitroService;
        this.partidoService = partidoService;
        this.usuarioService = usuarioService;
        this.reseñaService = reseñaService;
    }

    // ========== ENDPOINTS WEB (TEMPLATES) ==========


    @GetMapping
    public String dashboard(Model model){
        Optional<Arbitro> arbitro = arbitroService.findByUsername(usuarioService.getCurrentUsername());

        if (arbitro.isPresent()) {
            model.addAttribute("titulo", "Dashboard");
            model.addAttribute("nombre", arbitro.get().getNombre());
            model.addAttribute("arbitro", arbitro.get());
            Map<String, Object> estadisticas = partidoService.getEstadisticasArbitro(arbitro.get());
            model.addAttribute("estadisticas", estadisticas);
            
            // Agregar próximos partidos
            model.addAttribute("proximosPartidos", partidoService.findFuturePartidosByArbitro(arbitro.get()));
        }
        
        return "arbitro/dashboard";        
    }

    @GetMapping("/arbitros")
    public String index(Model model) {
        model.addAttribute("arbitros", arbitroService.findAll());
        return "arbitro/peril";
    }

    @GetMapping("/arbitro/perfil")
    public String perfil(Model model) {
        Optional<Arbitro> arbitro = arbitroService.findByUsername(usuarioService.getCurrentUsername());
        
        if (arbitro.isPresent()) {
            model.addAttribute("arbitro", arbitro.get());
            return "arbitro/perfil";
        }
        
        return "arbitro/perfil";
    }

    @GetMapping("/arbitros/{id}")
    public String show(@PathVariable Long id, Model model) {
        
        Optional<Arbitro> arbitro = arbitroService.findByUsername(usuarioService.getCurrentUsername());
        
        if (arbitro.isPresent()) {
            model.addAttribute("arbitro", arbitro.get());
            
            // Agregar estadísticas del árbitro
            Map<String, Object> estadisticas = partidoService.getEstadisticasArbitro(arbitro.get());
            model.addAttribute("estadisticas", estadisticas);
            
            return "arbitro/show";
        }
        
        // Si no se encuentra el árbitro, redirigir a la lista
        return "redirect:/arbitros";
  }
    @GetMapping("/lop")
    public ResponseEntity<Map<String, Object>> show(Model model) {
        Map<String, Object> response = new HashMap<>();
        Optional<Arbitro> arbitro = arbitroService.findByUsername(usuarioService.getCurrentUsername());
        //YearMonth yearMonth = yearMonth.now();
        Map<String, Object> arbri = new HashMap<>();
        arbri.put("arbitro", arbitro.orElse(null));
        return ResponseEntity.ok(arbri);
    }
  
    @GetMapping("/calendario")
    public String calendario(@RequestParam(required = false) Integer year,
                             @RequestParam(required = false) Integer month,
                             Model model) {
        Optional<Arbitro> arbitroOpt = arbitroService.findByUsername(usuarioService.getCurrentUsername());

        if (!arbitroOpt.isPresent()) {
            return "redirect:/arbitros";
        }
        
        Arbitro arbitro = arbitroOpt.get();
        
        // Si no se especifica año/mes, usar el actual
        YearMonth yearMonth = (year != null && month != null) 
            ? YearMonth.of(year, month) 
            : YearMonth.now();
        
        // Obtener datos del calendario
        Map<String, Object> calendarioData = partidoService.getCalendarioDataByArbitro(arbitro, yearMonth);
        
        // NUEVO: Obtener estadísticas globales del árbitro (todos los partidos)
        Map<String, Object> estadisticasGlobales = partidoService.getEstadisticasArbitro(arbitro);
        
        model.addAttribute("arbitro", arbitro);
        model.addAttribute("calendarioData", calendarioData);
        model.addAttribute("estadisticasGlobales", estadisticasGlobales);
        model.addAttribute("currentYear", yearMonth.getYear());
        model.addAttribute("currentMonth", yearMonth.getMonthValue());
        model.addAttribute("yearMonth", yearMonth);
        
        return "arbitro/calendario";
    }

    @GetMapping("/perfil")
    public String perfil(Model model) {
        Optional<Arbitro> arbitroOpt = arbitroService.findByUsername(usuarioService.getCurrentUsername());

        if (!arbitroOpt.isPresent()) {
            return "redirect:/arbitros";
        }
        
        Arbitro arbitro = arbitroOpt.get();
        
        model.addAttribute("arbitro", arbitro);
        
        return "arbitro/perfil";
    }

    @GetMapping("/reseñas")
    public String reseñas(Model model) {
        Optional<Arbitro> arbitroOpt = arbitroService.findByUsername(usuarioService.getCurrentUsername());

        if (!arbitroOpt.isPresent()) {
            return "redirect:/arbitros";
        }
        
        Arbitro arbitro = arbitroOpt.get();
        
        // Obtener todas las reseñas del árbitro
        model.addAttribute("arbitro", arbitro);
        model.addAttribute("reseñas", reseñaService.findReseñasByArbitro(arbitro));
        model.addAttribute("estadisticasReseñas", reseñaService.getEstadisticasReseñas(arbitro));
        
        return "arbitro/reseñas";
    }
    
    @GetMapping("/partidos")
    public String partidos(Model model) {
        Optional<Arbitro> arbitroOpt = arbitroService.findByUsername(usuarioService.getCurrentUsername());

        if (!arbitroOpt.isPresent()) {
            return "redirect:/arbitros";
        }
        
        Arbitro arbitro = arbitroOpt.get();
        
        // Obtener partidos ordenados usando el servicio
        List<Partido> partidosPasados = partidoService.findPartidosPasadosOrdenados(arbitro);
        List<Partido> partidosFuturos = partidoService.findPartidosFuturosOrdenados(arbitro);
        List<Partido> todosLosPartidos = partidoService.findByArbitro(arbitro);
        
        model.addAttribute("arbitro", arbitro);
        model.addAttribute("partidosPasados", partidosPasados);
        model.addAttribute("partidosFuturos", partidosFuturos);
        model.addAttribute("totalPartidos", todosLosPartidos.size());
        
        return "arbitro/partidos";
    }
    
    // API endpoint para obtener datos del calendario (AJAX)
    @GetMapping("/lop1")
    public ResponseEntity<Map<String, Object>> getCalendarioData(
            @PathVariable Long id,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {
        
        Optional<Arbitro> arbitroOpt = arbitroService.findById(id);
        
        if (!arbitroOpt.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        
        YearMonth yearMonth = (year != null && month != null) 
            ? YearMonth.of(year, month) 
            : YearMonth.now();
        
        Map<String, Object> calendarioData = partidoService.getCalendarioDataByArbitro(arbitroOpt.get(), yearMonth);
        
        return ResponseEntity.ok(calendarioData);
    }


    // ========== ENDPOINTS REST API (JSON/BLOB) ==========

    /**
     *  ENDPOINT ESENCIAL: Servir imágenes BLOB desde la base de datos
     */
    @GetMapping("/api/arbitros/{id:[0-9]+}/photo")
    @ResponseBody
    public ResponseEntity<byte[]> getArbitroPhoto(@PathVariable Long id) {
        return arbitroService.buildPhotoResponse(id);
    }

}