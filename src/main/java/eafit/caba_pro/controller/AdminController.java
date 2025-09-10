package eafit.caba_pro.controller;

import java.nio.charset.StandardCharsets;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.http.HttpHeaders;

import org.springframework.http.MediaType;
import org.springframework.dao.DataIntegrityViolationException;
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
import eafit.caba_pro.model.Escalafon;
import eafit.caba_pro.repository.EscalafonRepository;
import eafit.caba_pro.service.LiquidacionService;
import eafit.caba_pro.model.Liquidacion;
import eafit.caba_pro.model.Equipo;
import eafit.caba_pro.model.Partido;
import eafit.caba_pro.model.Usuario;
import eafit.caba_pro.service.ArbitroService;
import eafit.caba_pro.service.EquipoService;
import eafit.caba_pro.service.EntrenadorService;
import eafit.caba_pro.model.Entrenador;
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
    private final EquipoService equipoService;
    private final EntrenadorService entrenadorService;
    private final EscalafonRepository escalafonRepository;

    public AdminController(EntrenadorService entrenadorService, EquipoService equipoService, PdfService pdfService ,LiquidacionService liquidacionService, ArbitroService arbitroService, PartidoService partidoService, EscalafonRepository escalafonRepository) {
        this.arbitroService = arbitroService;
        this.partidoService = partidoService;
        this.liquidacionService = liquidacionService;
        this.pdfGeneratorService = pdfService;
        this.equipoService = equipoService;
        this.entrenadorService = entrenadorService;
        this.escalafonRepository = escalafonRepository;
    }

    
    @GetMapping()
    public String dashboard(Model model) {
        long totalPartidos = partidoService.count();
        long programados = partidoService.countByEstado(Partido.EstadoPartido.PROGRAMADO);
        long finalizados = partidoService.countByEstado(Partido.EstadoPartido.FINALIZADO);
        long pendientesConfirmacion = partidoService.countByEstado(Partido.EstadoPartido.PENDIENTE_CONFIRMACION);
        long arbitroNoDisponible = partidoService.countByEstado(Partido.EstadoPartido.ARBITRO_NO_DISPONIBLE);

        double porcentajeProgramados = totalPartidos > 0 ? (programados * 100.0 / totalPartidos) : 0;
        double porcentajeFinalizados = totalPartidos > 0 ? (finalizados * 100.0 / totalPartidos) : 0;
        double porcentajePendientes = totalPartidos > 0 ? (pendientesConfirmacion * 100.0 / totalPartidos) : 0;
        double porcentajeNoDisponible = totalPartidos > 0 ? (arbitroNoDisponible * 100.0 / totalPartidos) : 0;

        List<Arbitro> topArbitros = arbitroService.findTop5ActivosDelMes();
        long totalEntrenadores = entrenadorService.countActive();

        model.addAttribute("porcentajeProgramados", porcentajeProgramados);
        model.addAttribute("porcentajeFinalizados", porcentajeFinalizados);
        model.addAttribute("porcentajePendientes", porcentajePendientes);
        model.addAttribute("porcentajeNoDisponible", porcentajeNoDisponible);
        model.addAttribute("pendientesConfirmacion", pendientesConfirmacion);
        model.addAttribute("arbitroNoDisponible", arbitroNoDisponible);
        model.addAttribute("topArbitros", topArbitros);
        model.addAttribute("totalEntrenadores", totalEntrenadores);

        return "admin/dashboard";
    }

    // ==================== ÁRBITROS ====================

    @GetMapping("/arbitros")
    public String arbitros(Model model) {
        model.addAttribute("arbitros", arbitroService.findAll());
        return "admin/arbitros";
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

    @GetMapping("/arbitros/create")
    public String createArbitro(Model model) {
        Arbitro arbitro = new Arbitro();
        // Inicializar el usuario para evitar el error
        arbitro.setUsuario(new Usuario());
        
        model.addAttribute("arbitro", arbitro);
        model.addAttribute("escalafones", escalafonRepository.findAll());
        return "admin/createArbitro"; 
    }


    @PostMapping("/arbitros/save")
    public String saveArbitro(@Valid @ModelAttribute Arbitro arbitro, 
                            BindingResult result,
                            @RequestParam(value = "photoFile", required = false) MultipartFile photoFile,
                            Model model,
                            RedirectAttributes redirectAttributes) {
        try {
            if (result.hasErrors()) {
                model.addAttribute("escalafones", escalafonRepository.findAll());
                return "admin/createArbitro"; // Changed to match your template
            }

            arbitroService.createArbitroWithPhoto(arbitro, photoFile);
            redirectAttributes.addFlashAttribute("successMessage", "Árbitro creado exitosamente");
            return "redirect:/admin/arbitros";
            
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("escalafones", escalafonRepository.findAll());
            return "admin/createArbitro"; // Changed to match your template
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error al crear el árbitro: " + e.getMessage());
            model.addAttribute("escalafones", escalafonRepository.findAll());
            return "admin/createArbitro"; // Changed to match your template
        }
    }

    @GetMapping("/arbitros/edit/{id}")
    public String editArbitro(@PathVariable Long id, Model model) {
        Arbitro arbitro = arbitroService.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Árbitro no encontrado"));
        // Asegura objeto usuario para el form, si lo usas ahí
        if (arbitro.getUsuario() == null) {
            arbitro.setUsuario(new Usuario());
        }
        model.addAttribute("arbitro", arbitro);
        model.addAttribute("escalafones", escalafonRepository.findAll());
        model.addAttribute("modoEdicion", true);
        return "admin/arbitro_form"; // Changed to use the proper form template
    }

    @GetMapping("/arbitros/{id}/photo")
    public ResponseEntity<byte[]> getArbitroPhoto(@PathVariable Long id) {
        try {
            Arbitro arbitro = arbitroService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Árbitro no encontrado"));
            
            if (!arbitro.hasPhoto()) {
                return ResponseEntity.notFound().build();
            }
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(arbitro.getPhotoContentType()));
            headers.setContentLength(arbitro.getPhotoData().length);
            
            return ResponseEntity.ok()
                .headers(headers)
                .body(arbitro.getPhotoData());
                
        } catch (Exception e) {
            System.err.println("Error al obtener foto del árbitro " + id + ": " + e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/arbitros/update/{id}")
    public String updateArbitro(@PathVariable Long id,
                               @Valid @ModelAttribute("arbitro") Arbitro arbitro,
                               BindingResult result,
                               @RequestParam(value = "photoFile", required = false) MultipartFile photoFile,
                               @RequestParam(value = "removePhoto", defaultValue = "false") boolean removePhoto,
                               @RequestParam(value = "updatePassword", defaultValue = "false") boolean updatePassword,
                               Model model, RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            model.addAttribute("errorMessage", "Por favor corrige los errores en el formulario");
            model.addAttribute("escalafones", escalafonRepository.findAll());
            model.addAttribute("modoEdicion", true);
            return "admin/arbitro_form";
        }

        try {
            // Set the ID to ensure we're updating the correct arbitro
            arbitro.setId(id);
            
            Arbitro updatedArbitro = arbitroService.updateArbitroWithPhoto(arbitro, photoFile, removePhoto, updatePassword);

            redirectAttributes.addFlashAttribute("successMessage", 
                "¡Árbitro '" + updatedArbitro.getNombre() + "' actualizado exitosamente!");

            return "redirect:/admin/arbitros";
            
        } catch (RuntimeException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("escalafones", escalafonRepository.findAll());
            model.addAttribute("modoEdicion", true);
            return "admin/arbitro_form";
        } catch (Exception e) {
            System.err.println("Error al actualizar árbitro: " + e.getMessage());
            e.printStackTrace();
            
            model.addAttribute("errorMessage", "Error al actualizar el árbitro. Por favor intenta nuevamente.");
            model.addAttribute("escalafones", escalafonRepository.findAll());
            model.addAttribute("modoEdicion", true);
            return "admin/arbitro_form";
        }
    }

    @PostMapping("/arbitros/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Optional<Arbitro> arbitro = arbitroService.findById(id);
            String nombre = arbitro.map(Arbitro::getNombre).orElse(null);
            
            boolean deleted = arbitroService.deleteById(id);
            if (deleted) {
                redirectAttributes.addFlashAttribute("successMessage",
                    "¡Árbitro '" + nombre + "' eliminado exitosamente!");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage",
                    "No se pudo encontrar el árbitro a eliminar.");
            }
        } catch (DataIntegrityViolationException e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                "No se puede eliminar porque tiene partidos asociados. " +
                "Desasigna o elimina sus partidos primero.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                "Error interno al eliminar el árbitro. Por favor intenta nuevamente.");
            System.err.println("Error al eliminar árbitro con ID " + id + ": " + e.getMessage());
        }
        return "redirect:/admin/arbitros";
    }

  
    @GetMapping("/arbitros/{id}")
    public String viewArbitro(@PathVariable Long id, Model model) {
        Arbitro arbitro = arbitroService.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Árbitro no encontrado"));
        model.addAttribute("arbitro", arbitro);
        return "admin/arbitro_view"; // ver plantilla en el punto 4
    }
  
    // ==================== COACHES/ENTRENADORES ====================


    @GetMapping("/coaches")
    public String listarCoaches(Model model) {
        List<Entrenador> coaches = entrenadorService.findAllActive();
        model.addAttribute("coaches", coaches);
        return "admin/coaches";
    }

    /**
     * Mostrar formulario para crear un nuevo coach
     * Ruta: /admin/coaches/create
     */
    @GetMapping("/coaches/create")
    public String mostrarFormularioCrearCoach(Model model) {
        model.addAttribute("entrenador", new Entrenador());
        model.addAttribute("categorias", Entrenador.Categoria.values());
        return "admin/coaches_form";
    }

    /**
     * Procesar la creación de un nuevo coach
     * Ruta: POST /admin/coaches/save
     */
    @PostMapping("/coaches/save")
    public String guardarCoach(
            @Valid @ModelAttribute("entrenador") Entrenador entrenador,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("categorias", Entrenador.Categoria.values());
            model.addAttribute("errorMessage", "Por favor corrige los errores en el formulario");
            return "admin/coaches_form";
        }

        try {
            // Validar que no exista un coach con la misma cédula
            if (entrenadorService.existsByCedula(entrenador.getCedula())) {
                model.addAttribute("errorMessage", "Ya existe un entrenador con esa cédula");
                model.addAttribute("categorias", Entrenador.Categoria.values());
                return "admin/coaches_form";
            }

            // Validar que no exista un coach con el mismo email
            if (entrenadorService.existsByEmail(entrenador.getEmail())) {
                model.addAttribute("errorMessage", "Ya existe un entrenador con ese email");
                model.addAttribute("categorias", Entrenador.Categoria.values());
                return "admin/coaches_form";
            }

            // Crear el coach con usuario y contraseña por defecto
            Entrenador savedCoach = entrenadorService.createCoachWithUser(entrenador);

            redirectAttributes.addFlashAttribute("successMessage", 
                "¡Coach '" + savedCoach.getNombreCompleto() + "' creado exitosamente!");

            return "redirect:/admin/coaches";

        } catch (RuntimeException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("categorias", Entrenador.Categoria.values());
            return "admin/coaches_form";
        } catch (Exception e) {
            System.err.println("Error al crear coach: " + e.getMessage());
            e.printStackTrace();
            
            model.addAttribute("errorMessage", "Error al crear el coach. Por favor intenta nuevamente.");
            model.addAttribute("categorias", Entrenador.Categoria.values());
            return "admin/coaches_form";
        }
    }

    /**
     * Ver detalles completos de un coach
     * Ruta: /admin/coaches/view/{id}
     */
    @GetMapping("/coaches/view/{id}")
    public String verCoach(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Entrenador> coachOpt = entrenadorService.findById(id);
        
        if (coachOpt.isPresent()) {
            Entrenador coach = coachOpt.get();
            
            // Obtener estadísticas del coach
            int partidosJugados = partidoService.countPartidosByEquipo(coach.getEquipo());
            int partidosGanados = partidoService.countPartidosGanadosByEquipo(coach.getEquipo());
            var resenasRecientes = entrenadorService.findReseñasByEntrenador(id);
            
            model.addAttribute("coach", coach);
            model.addAttribute("partidosJugados", partidosJugados);
            model.addAttribute("partidosGanados", partidosGanados);
            model.addAttribute("reseñasRecientes", resenasRecientes);
            
            return "admin/coaches_view";
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Coach no encontrado");
            return "redirect:/admin/coaches";
        }
    }

    /**
     * Mostrar formulario para editar un coach
     * Ruta: /admin/coaches/edit/{id}
     */
    @GetMapping("/coaches/edit/{id}")
    public String mostrarFormularioEditarCoach(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Entrenador> coachOpt = entrenadorService.findById(id);
        
        if (coachOpt.isPresent()) {
            model.addAttribute("entrenador", coachOpt.get());
            model.addAttribute("categorias", Entrenador.Categoria.values());
            model.addAttribute("editMode", true);
            return "admin/coaches_form";
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Coach no encontrado");
            return "redirect:/admin/coaches";
        }
    }

    /**
     * Procesar la edición de un coach
     * Ruta: POST /admin/coaches/update/{id}
     */
    @PostMapping("/coaches/update/{id}")
    public String actualizarCoach(
            @PathVariable("id") Long id,
            @Valid @ModelAttribute("entrenador") Entrenador entrenadorActualizado,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("categorias", Entrenador.Categoria.values());
            model.addAttribute("editMode", true);
            model.addAttribute("errorMessage", "Por favor corrige los errores en el formulario");
            return "admin/coaches_form";
        }

        try {
            Optional<Entrenador> coachOpt = entrenadorService.findById(id);
            
            if (coachOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Coach no encontrado");
                return "redirect:/admin/coaches";
            }

            Entrenador coachExistente = coachOpt.get();
            
            // Validar cédula única (excluyendo el coach actual)
            if (!coachExistente.getCedula().equals(entrenadorActualizado.getCedula()) &&
                entrenadorService.existsByCedula(entrenadorActualizado.getCedula())) {
                model.addAttribute("errorMessage", "Ya existe un entrenador con esa cédula");
                model.addAttribute("categorias", Entrenador.Categoria.values());
                model.addAttribute("editMode", true);
                return "admin/coaches_form";
            }

            // Validar email único (excluyendo el coach actual)
            if (!coachExistente.getEmail().equals(entrenadorActualizado.getEmail()) &&
                entrenadorService.existsByEmail(entrenadorActualizado.getEmail())) {
                model.addAttribute("errorMessage", "Ya existe un entrenador con ese email");
                model.addAttribute("categorias", Entrenador.Categoria.values());
                model.addAttribute("editMode", true);
                return "admin/coaches_form";
            }

            // Actualizar los campos del coach
            entrenadorActualizado.setId(id);
            entrenadorService.updateCoach(entrenadorActualizado);

            redirectAttributes.addFlashAttribute("successMessage", 
                "¡Coach '" + entrenadorActualizado.getNombreCompleto() + "' actualizado correctamente!");

            return "redirect:/admin/coaches/view/" + id;

        } catch (RuntimeException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("categorias", Entrenador.Categoria.values());
            model.addAttribute("editMode", true);
            return "admin/coaches_form";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al actualizar el coach");
            return "redirect:/admin/coaches";
        }
    }

    /**
     * Eliminar un coach
     * Ruta: POST /admin/coaches/delete/{id}
     */
    @PostMapping("/coaches/delete/{id}")
    public String eliminarCoach(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Optional<Entrenador> coachOpt = entrenadorService.findById(id);
            String coachName = coachOpt.map(Entrenador::getNombreCompleto).orElse(null);
            
            boolean deleted = entrenadorService.deleteById(id);
            
            if (deleted) {
                redirectAttributes.addFlashAttribute("successMessage", 
                    "¡Coach" + (coachName != null ? " '" + coachName + "'" : "") + " eliminado exitosamente!");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", 
                    "No se pudo encontrar el coach a eliminar.");
            }
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Error interno al eliminar el coach. Por favor intenta nuevamente.");
            System.err.println("Error al eliminar coach con ID " + id + ": " + e.getMessage());
        }
        
        return "redirect:/admin/coaches";
    }

    // ==================== FUNCIONALIDADES ORIGINALES ====================
    
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

    // -------------------- PARTIDOS --------------------

    @GetMapping("/partidos")
    public String partidos(Model model) {
        List<Partido> partidos = partidoService.findAll();
        model.addAttribute("partidos", partidos);
        return "admin/partidos";
    }

    @GetMapping("/lop")
    public ResponseEntity<Map<String, Object>> show(Model model) {
        //Map<String, Object> response = new HashMap<>();

        Optional<Partido> partidos = partidoService.findById(1L);
        Map<String, Object> arbri = new HashMap<>();
        arbri.put("partidos", partidos.get());
        return ResponseEntity.ok(arbri);
    }

    @GetMapping("/partidos/create")
    public String mostrarFormularioCrear(Model model) {
        Partido p = new Partido();
        // Evita null al evaluar *{arbitro.id} y *{equipo*.id} en la vista
        p.setArbitro(new Arbitro());
        p.setEquipoLocal(new Equipo());
        p.setEquipoVisitante(new Equipo());

        model.addAttribute("partido", p);
        // Si no usas @ModelAttribute (ver abajo), entonces añade estas 3 líneas:
        model.addAttribute("arbitros", arbitroService.findAll());
        model.addAttribute("equipos",  equipoService.findAll());
        model.addAttribute("estados",  Partido.EstadoPartido.values());

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
        Partido partido = partidoService.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Partido no encontrado"));
        model.addAttribute("partido", partido);
        model.addAttribute("arbitros", arbitroService.findAll());
        model.addAttribute("equipos", equipoService.findAll());       // <-- necesario
        model.addAttribute("estados", Partido.EstadoPartido.values());
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