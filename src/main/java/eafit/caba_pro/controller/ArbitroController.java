package eafit.caba_pro.controller;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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


@Controller
@RequestMapping("/arbitro")
public class ArbitroController {

    private final ArbitroService arbitroService;
    private final PartidoService partidoService;
    private final UsuarioService usuarioService;

    public ArbitroController(ArbitroService arbitroService, PartidoService partidoService, UsuarioService usuarioService) {
        this.arbitroService = arbitroService;
        this.partidoService = partidoService;
        this.usuarioService = usuarioService;
    }

    // ========== ENDPOINTS WEB (TEMPLATES) ==========


    @GetMapping
    public String dashboard(Model model){
        Optional<Arbitro> arbitro = arbitroService.findByUsername(usuarioService.getCurrentUsername());

        model.addAttribute("titulo", "Dashboard");
        model.addAttribute("nombre", arbitro.get().getNombre());
        model.addAttribute("arbitro", arbitro.get());
        Map<String, Object> estadisticas = partidoService.getEstadisticasArbitro(arbitro.get());
        model.addAttribute("estadisticas", estadisticas);
        return "arbitro/dashboard";
    }

    @GetMapping("/arbitros")
    public String index(Model model) {
        model.addAttribute("arbitros", arbitroService.findAll());
        return "arbitro/index";
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
        
        model.addAttribute("arbitro", arbitro);
        model.addAttribute("calendarioData", calendarioData);
        model.addAttribute("currentYear", yearMonth.getYear());
        model.addAttribute("currentMonth", yearMonth.getMonthValue());
        model.addAttribute("yearMonth", yearMonth);
        
        return "arbitro/calendario";
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
        try {
            System.out.println(" Solicitando imagen para árbitro ID: " + id);
            
            Optional<Arbitro> arbitroOptional = arbitroService.findById(id);
            
            if (arbitroOptional.isPresent()) {
                Arbitro arbitro = arbitroOptional.get();
                
                if (arbitro.hasPhoto()) {
                    HttpHeaders headers = new HttpHeaders();
                    
                    // Establecer tipo de contenido
                    String contentType = arbitro.getPhotoContentType();
                    if (contentType != null) {
                        headers.setContentType(MediaType.parseMediaType(contentType));
                    } else {
                        headers.setContentType(MediaType.IMAGE_JPEG); // Por defecto
                    }
                    
                    // Configurar cache
                    headers.setCacheControl("max-age=3600"); // Cache por 1 hora
                    
                    // Nombre del archivo para descarga (opcional)
                    if (arbitro.getPhotoFilename() != null) {
                        headers.setContentDispositionFormData("inline", arbitro.getPhotoFilename());
                    }
                    
                    System.out.println("Sirviendo imagen: " + arbitro.getNombre() + 
                                     " (" + arbitro.getPhotoData().length + " bytes, " + contentType + ")");
                    
                    return new ResponseEntity<>(arbitro.getPhotoData(), headers, HttpStatus.OK);
                } else {
                    System.out.println("Árbitro " + arbitro.getNombre() + " no tiene imagen");
                    return ResponseEntity.notFound().build();
                }
            } else {
                System.out.println(" Árbitro no encontrado con ID: " + id);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            System.err.println(" Error al servir imagen para árbitro ID " + id + ": " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

}