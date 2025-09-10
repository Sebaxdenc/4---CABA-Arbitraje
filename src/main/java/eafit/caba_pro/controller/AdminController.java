package eafit.caba_pro.controller;

import java.nio.charset.StandardCharsets;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.http.HttpHeaders;

import org.springframework.http.MediaType;
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
import eafit.caba_pro.model.Liquidacion;
import eafit.caba_pro.model.Partido;
import eafit.caba_pro.model.Usuario;
import eafit.caba_pro.service.ArbitroService;
import eafit.caba_pro.service.LiquidacionService;
import eafit.caba_pro.service.PartidoService;
import eafit.caba_pro.service.PdfService;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final ArbitroService arbitroService;
    private final PartidoService partidoService;
    private final LiquidacionService liquidacionService;
    private final PdfService pdfGeneratorService;


    public AdminController(PdfService pdfService ,LiquidacionService liquidacionService, ArbitroService arbitroService, PartidoService partidoService) {
        this.arbitroService = arbitroService;
        this.partidoService = partidoService;
        this.liquidacionService = liquidacionService;
        this.pdfGeneratorService = pdfService;
    }
    
    @GetMapping()
    public String dashboard(Model model) {
        long totalPartidos = partidoService.count();
        long programados = partidoService.countByEstado(Partido.EstadoPartido.PROGRAMADO);
        long finalizados = partidoService.countByEstado(Partido.EstadoPartido.FINALIZADO);

        double porcentajeProgramados = totalPartidos > 0 ? (programados * 100.0 / totalPartidos) : 0;
        double porcentajeFinalizados = totalPartidos > 0 ? (finalizados * 100.0 / totalPartidos) : 0;

        List<Arbitro> topArbitros = arbitroService.findTop5ActivosDelMes();

        model.addAttribute("porcentajeProgramados", porcentajeProgramados);
        model.addAttribute("porcentajeFinalizados", porcentajeFinalizados);
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

    @GetMapping("/liquidaciones")
    public String listarLiquidaciones(String periodo, Model model) {

        // Periodo por defecto = mes actual
        YearMonth periodoSeleccionado;
        if (periodo != null) {
            periodoSeleccionado = YearMonth.parse(periodo); // formato "2025-09"
        } else {
            periodoSeleccionado = YearMonth.now();
        }
        
        // Liquidaciones de ese periodo
        List<Liquidacion> liquidaciones = liquidacionService.obtenerLiquidacionesPorPeriodo(periodoSeleccionado);

        // Pasar al modelo
        model.addAttribute("liquidaciones", liquidaciones);
        model.addAttribute("periodoSeleccionado", periodoSeleccionado);

        return "admin/liquidaciones";
    }

    @PostMapping("/liquidaciones/generar")
    public String generarLiquidaciones(@RequestParam String periodo) {
        YearMonth ym = YearMonth.parse(periodo); // formato "YYYY-MM"
        liquidacionService.generarLiquidacionesMensuales(ym);
        return "redirect:/admin/liquidaciones?periodo=" + ym;
    }

    @PostMapping("/liquidaciones/{id}/pagar")
    public String pagar(@PathVariable Long id) {
        Optional<Liquidacion> opt = liquidacionService.findById(id);
        if (opt.isPresent() && opt.get().getEstado() == Liquidacion.EstadoLiquidacion.PENDIENTE) {
            liquidacionService.marcarComoPagada(id);
        }
        return "redirect:/admin/liquidaciones";
    }

    @GetMapping("/liquidaciones/{id}")
    public ResponseEntity<byte[]> generarPdf(@PathVariable Long id) {
        Liquidacion liq = liquidacionService.obtenerPorId(id);
        byte[] pdf = pdfGeneratorService.generarPdfDesdeLiquidacion(liq);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=liquidacion-" + id + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
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

