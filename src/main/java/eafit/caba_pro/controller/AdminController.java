package eafit.caba_pro.controller;

import java.util.Optional;

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
    public String dashboard() {
        return "/admin/dashboard";
    }

    @GetMapping("/arbitros")
    public String arbitros(Model model) {
        model.addAttribute("arbitros", arbitroService.findAll());
        return "admin/arbitros";
    }

    @GetMapping("/partidos")
    public String partidos(Model model) {
        model.addAttribute("partidos", partidoService.findAll());
        return "admin/partidos";
    }
    
    // Finanzas (puedes pasar reporte ya calculado desde un service propio)
    @GetMapping("/finanzas")
    public String finanzas(
            @RequestParam(name = "mes", required = false) Integer mes,
            @RequestParam(name = "anio", required = false) Integer anio,
            Model model) {

        // placeholders: usa tus propios services si los tienes
        model.addAttribute("mesActual", "Septiembre");
        model.addAttribute("reporte", java.util.Collections.emptyList());
        return "admin/finanzas";
    }

    // Reportes (form)
    @GetMapping("/reportes")
    public String reportes(
            @RequestParam(name = "mes", required = false) Integer mes,
            @RequestParam(name = "anio", required = false) Integer anio,
            Model model) {

        model.addAttribute("mes", mes);
        model.addAttribute("anio", anio);
        return "admin/reportes";
    }

    // (Opcional) Endpoint para descargar/generar PDF
    @GetMapping("/reportes/pdf")
    public String generarReportePdf(
            @RequestParam("mes") Integer mes,
            @RequestParam("anio") Integer anio,
            Model model) {

        // Aquí invocas tu servicio que genera el PDF o arma el model
        // model.addAttribute("fileUrl", "…");
        model.addAttribute("mes", mes);
        model.addAttribute("anio", anio);
        // Puedes redirigir a un controlador que sirva el PDF o mostrar un mensaje
        return "admin/reportes"; // o redirigir con redirect:/admin/reportes
    }

    @GetMapping("/create")
    public String create(Model model) {
        model.addAttribute("arbitro", new Arbitro());
        return "admin/create";
    }

    // Crear árbitro con foto

    @PostMapping("/arbitros/save")
    public String save(@Valid @ModelAttribute("arbitro") Arbitro arbitro,
                      BindingResult result,
                      @RequestParam(value = "photoFile", required = false) MultipartFile photoFile,
                      Model model, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("errorMessage", "Por favor corrige los errores en el formulario");
            return "admin/create";
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


}

