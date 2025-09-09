package eafit.caba_pro.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import eafit.caba_pro.model.Arbitro;
import eafit.caba_pro.model.Partido;
import eafit.caba_pro.model.Usuario;
import eafit.caba_pro.service.ArbitroService;
import eafit.caba_pro.service.PartidoService;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final ArbitroService arbitroService;
    private final PartidoService partidoService;


    public AdminController(ArbitroService arbitroService, PartidoService partidoService) {
        this.arbitroService = arbitroService;
        this.partidoService = partidoService;
    }
    
    @GetMapping()
    public String dashboard(Model model) {
        long totalPartidos = partidoService.count();
        long aceptados = partidoService.countByEstado(Partido.EstadoPartido.PROGRAMADO);
        long rechazados = partidoService.countByEstado(Partido.EstadoPartido.CANCELADO);

        double porcentajeAceptados = totalPartidos > 0 ? (aceptados * 100.0 / totalPartidos) : 0;
        double porcentajeRechazados = totalPartidos > 0 ? (rechazados * 100.0 / totalPartidos) : 0;

        List<Arbitro> topArbitros = arbitroService.findTop5ActivosDelMes();

        model.addAttribute("porcentajeAceptados", porcentajeAceptados);
        model.addAttribute("porcentajeRechazados", porcentajeRechazados);
        model.addAttribute("topArbitros", topArbitros);

        return "admin/dashboard";
    }

    @GetMapping("/arbitros")
    public String arbitros(Model model) {
        model.addAttribute("arbitros", arbitroService.findAll());
        return "admin/arbitros";
    }
    
    @GetMapping("/finanzas")
    public String finanzas() {
        return "admin/finanzas";
    }

    @GetMapping("/reportes")
    public String reportes(
            @RequestParam(name = "mes", required = false) Integer mes,
            @RequestParam(name = "anio", required = false) Integer anio,
            Model model) {

        model.addAttribute("mes", mes);
        model.addAttribute("anio", anio);
        return "admin/reportes";
    }


    @GetMapping("/reportes/pdf")
    public String generarReportePdf(
            @RequestParam("mes") Integer mes,
            @RequestParam("anio") Integer anio,
            Model model) {

        model.addAttribute("mes", mes);
        model.addAttribute("anio", anio);

        return "admin/reportes"; 
    }

    @GetMapping("/create")
    public String create(Model model) {
        Arbitro arbitro = new Arbitro();
        arbitro.setUsuario(new Usuario()); 
        model.addAttribute("arbitro", arbitro);
        return "admin/create";
    }

    // Crear árbitro con foto

    @PostMapping("/arbitros/save")
    public String save(@Valid @ModelAttribute("arbitro") Arbitro arbitro,
                      BindingResult result,
                      @RequestParam(value = "photoFile", required = false) MultipartFile photoFile,
                      Model model, RedirectAttributes redirectAttributes) {

                            result.getAllErrors().forEach(err -> System.out.println(err.toString()));
        if (result.hasErrors()) {
            model.addAttribute("errorMessage", "Por favor corrige los errores en el formulario");
            return "admin/create";
        }

        try {

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

            return "redirect:/admin/arbitros";
            
        } catch (RuntimeException e) {
            // Errores de validación de negocio (cédula duplicada, etc.)
            model.addAttribute("errorMessage", e.getMessage());
            return "admin/create";
        } catch (Exception e) {
            System.err.println("Error al crear árbitro: " + e.getMessage());
            e.printStackTrace();
            
            model.addAttribute("errorMessage", "Error al crear el árbitro. Por favor intenta nuevamente.");
            return "admin/create";
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
        
        return "redirect:/admin/arbitros";
    }

    // -------------------- PARTIDOS --------------------

    @GetMapping("/partidos")
    public String partidos(Model model) {
        List<Partido> partidos = partidoService.findAll();
        model.addAttribute("partidos", partidos);
        return "admin/partidos";
    }

    @GetMapping("/lop")
    public ResponseEntity<Map<String, Object>> show(Model model) {
        Map<String, Object> response = new HashMap<>();

        Optional<Partido> partidos = partidoService.findById(1L);
        Map<String, Object> arbri = new HashMap<>();
        arbri.put("partidos", partidos.get());
        return ResponseEntity.ok(arbri);
    }

    @GetMapping("/partidos/create")
    public String mostrarFormularioCrear(Model model) {
        model.addAttribute("partido", new Partido());
        model.addAttribute("arbitros", arbitroService.findAll());
        return "admin/partido_form";
    }


    @PostMapping("/partidos/save")
    public String guardar(@ModelAttribute Partido partido) {
        if (partido.getArbitro() != null && partido.getArbitro().getId() != null) {
            arbitroService.findById(partido.getArbitro().getId())
                        .ifPresent(partido::setArbitro);
        } else {
            partido.setArbitro(null);
        }
        partidoService.crearPartido(partido);
        return "redirect:/admin/partidos";
    }


    @GetMapping("/partidos/edit/{id}")
    public String editar(@PathVariable Long id, Model model) {
        Optional<Partido> partido = partidoService.findById(id);
        model.addAttribute("partido", partido);
        model.addAttribute("arbitros", arbitroService.findAll());
        return "admin/partido_form";
    }

    @PostMapping("/partidos/delete/{id}")
    public String deletePartido(@PathVariable Long id, RedirectAttributes ra) {
        try {
    
            Optional<Partido> opt = partidoService.findById(id);
            String label = opt.map(p -> p.getEquipoLocal().getNombre() + " vs " + p.getEquipoVisitante().getNombre())
                            .orElse("Partido #" + id);

            boolean deleted = partidoService.deleteById(id);

            if (deleted) {
                ra.addFlashAttribute("successMessage", "¡Partido \"" + label + "\" eliminado exitosamente!");
            } else {
                ra.addFlashAttribute("errorMessage", "No se encontró el partido a eliminar.");
            }
        } catch (RuntimeException ex) {
            
            ra.addFlashAttribute("errorMessage", ex.getMessage());
        } catch (Exception ex) {
            ra.addFlashAttribute("errorMessage", "Error interno al eliminar el partido. Intenta nuevamente.");
            System.err.println("Error al eliminar partido " + id + ": " + ex.getMessage());
        }
        return "redirect:/admin/partidos";
    }

}

