package eafit.caba_pro.controller;
import eafit.caba_pro.service.UsuarioService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import eafit.caba_pro.model.Arbitro;
import eafit.caba_pro.service.ArbitroService;
import eafit.caba_pro.service.PartidoService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import java.time.YearMonth;
import java.util.Map;
import java.util.Optional;



@Controller
@RequestMapping("/arbitro")
public class ArbitroController {

    private final ArbitroService arbitroService;
    private final PartidoService partidoService;

    public ArbitroController(ArbitroService arbitroService, PartidoService partidoService) {
        this.arbitroService = arbitroService;
        this.partidoService = partidoService;
    }

    // ========== ENDPOINTS WEB (TEMPLATES) ==========


    @GetMapping
    public String dashboard(){
        //return "dsa";
        return "arbitro/dashboard";
    }

    @GetMapping("/arbitros")
    public String index(Model model) {
        model.addAttribute("arbitros", arbitroService.findAll());
        return "arbitro/index";
    }

    @GetMapping("/arbitros/{id}")
    public String show(@PathVariable Long id, Model model) {
        Optional<Arbitro> arbitro = arbitroService.findById(id);
        
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
    
    @GetMapping("/arbitros/{id}/calendario")
    public String calendario(@PathVariable Long id, 
                           @RequestParam(required = false) Integer year,
                           @RequestParam(required = false) Integer month,
                           Model model) {
        Optional<Arbitro> arbitroOpt = arbitroService.findById(id);
        
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
    @GetMapping("/api/arbitros/{id}/calendario")
    @ResponseBody
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

    @GetMapping("/arbitros/create")
    public String create(Model model) {
        model.addAttribute("arbitro", new Arbitro());
        return "arbitro/create";
    }

    // Crear árbitro con foto

    @PostMapping("/arbitros/save")
    public String save(@Valid @ModelAttribute("arbitro") Arbitro arbitro,
                      @RequestParam(value = "photoFile", required = false) MultipartFile photoFile,
                      BindingResult result, Model model, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("errorMessage", "Por favor corrige los errores en el formulario");
            return "arbitro/create";
        }

        try {
            // Usar el service para crear con foto
            Arbitro savedArbitro = arbitroService.createArbitroWithPhoto(arbitro, photoFile);

            // Log para debugging
            System.out.println(" Árbitro guardado: " + savedArbitro.getNombre());
            System.out.println("- ID: " + savedArbitro.getId());
            System.out.println("- Tiene imagen: " + savedArbitro.hasPhoto());
            if (savedArbitro.hasPhoto()) {
                System.out.println("- Tamaño imagen: " + savedArbitro.getPhotoData().length + " bytes");
                System.out.println("- Tipo imagen: " + savedArbitro.getPhotoContentType());
                System.out.println("- URL imagen: " + savedArbitro.getPhotoUrl());
            }

            redirectAttributes.addFlashAttribute("successMessage", 
                "¡Árbitro '" + savedArbitro.getNombre() + "' creado exitosamente!");

            return "redirect:/arbitros";
            
        } catch (RuntimeException e) {
            // Errores de validación de negocio (cédula duplicada, etc.)
            model.addAttribute("errorMessage", e.getMessage());
            return "arbitro/create";
        } catch (Exception e) {
            System.err.println("Error al crear árbitro: " + e.getMessage());
            e.printStackTrace();
            
            model.addAttribute("errorMessage", "Error al crear el árbitro. Por favor intenta nuevamente.");
            return "arbitro/create";
        }
    }

    @PostMapping("/arbitros/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            // Buscar el árbitro para obtener su nombre antes de eliminar
            Optional<Arbitro> arbitro = arbitroService.findById(id);
            String arbitroName = arbitro.map(Arbitro::getNombre).orElse(null);
            
            boolean deleted = arbitroService.deleteById(id);
            
            if (deleted) {
                redirectAttributes.addFlashAttribute("successMessage", 
                    "¡Árbitro" + (arbitroName != null ? " '" + arbitroName + "'" : "") + " eliminado exitosamente!");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", 
                    "No se pudo encontrar el árbitro a eliminar.");
            }
        } catch (RuntimeException e) {
            // Errores de negocio (como integridad referencial)
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Error interno al eliminar el árbitro. Por favor intenta nuevamente.");
            System.err.println("Error al eliminar árbitro con ID " + id + ": " + e.getMessage());
        }
        
        return "redirect:/arbitros";
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
