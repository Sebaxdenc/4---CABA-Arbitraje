package eafit.caba_pro.controller;

import eafit.caba_pro.model.Partido;
import eafit.caba_pro.model.Arbitro;
import eafit.caba_pro.service.PartidoService;
import eafit.caba_pro.service.ArbitroService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/partido")             // posiblemente toque cambiar para acceder a aqui desde arbitro
public class PartidoController {

    private final PartidoService partidoService;
    private final ArbitroService arbitroService;

    public PartidoController(PartidoService partidoService, ArbitroService arbitroService) {
        this.partidoService = partidoService;
        this.arbitroService = arbitroService;
    }

    @GetMapping("/partidos")
    public String index(Model model) {
        model.addAttribute("partidos", partidoService.findAll());
        return "partido/index";
    }

    @GetMapping("/partidos/{id}")
    public String show(@PathVariable Long id, Model model) {
        Optional<Partido> partido = partidoService.findById(id);
        
        if (partido.isPresent()) {
            model.addAttribute("partido", partido.get());
            return "partido/show";
        }
        
        return "redirect:/partidos";
    }

    @GetMapping("/partidos/create")
    public String create(Model model) {
        model.addAttribute("partido", new Partido());
        model.addAttribute("arbitros", arbitroService.findAll());
        model.addAttribute("estados", Partido.EstadoPartido.values());
        return "partido/create";
    }

    @PostMapping("/partidos/save")
    public String save(@Valid @ModelAttribute("partido") Partido partido,
                      BindingResult result, Model model, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("arbitros", arbitroService.findAll());
            model.addAttribute("estados", Partido.EstadoPartido.values());
            model.addAttribute("errorMessage", "Por favor corrige los errores en el formulario");
            return "partido/create";
        }

        try {
            Partido savedPartido = partidoService.crearPartido(partido);
            
            redirectAttributes.addFlashAttribute("successMessage", 
                "¡Partido '" + savedPartido.getPartidoCompleto() + "' creado exitosamente!");

            return "redirect:/partidos";
            
        } catch (RuntimeException e) {
            model.addAttribute("arbitros", arbitroService.findAll());
            model.addAttribute("estados", Partido.EstadoPartido.values());
            model.addAttribute("errorMessage", e.getMessage());
            return "partido/create";
        } catch (Exception e) {
            model.addAttribute("arbitros", arbitroService.findAll());
            model.addAttribute("estados", Partido.EstadoPartido.values());
            model.addAttribute("errorMessage", "Error al crear el partido. Por favor intenta nuevamente.");
            return "partido/create";
        }
    }

    @PostMapping("/partidos/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Optional<Partido> partido = partidoService.findById(id);
            String partidoName = partido.map(Partido::getPartidoCompleto).orElse(null);
            
            boolean deleted = partidoService.deleteById(id);
            
            if (deleted) {
                redirectAttributes.addFlashAttribute("successMessage", 
                    "¡Partido" + (partidoName != null ? " '" + partidoName + "'" : "") + " eliminado exitosamente!");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", 
                    "No se pudo encontrar el partido a eliminar.");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Error al eliminar el partido. Por favor intenta nuevamente.");
        }
        
        return "redirect:/partidos";
    }

    // API endpoints
    @GetMapping("/api/arbitro/{arbitroId}")
    @ResponseBody
    public ResponseEntity<List<Partido>> getPartidosByArbitro(@PathVariable Long arbitroId) {
        Optional<Arbitro> arbitro = arbitroService.findById(arbitroId);
        
        if (!arbitro.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        
        List<Partido> partidos = partidoService.findPartidosByArbitro(arbitro.get());
        return ResponseEntity.ok(partidos);
    }
}
