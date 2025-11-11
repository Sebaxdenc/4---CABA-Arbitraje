package eafit.caba_pro.controller;

import java.nio.charset.StandardCharsets;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
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
import eafit.caba_pro.service.NotificacionService;
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
    private final NotificacionService notificacionService;

    @Autowired
    private MessageSource messageSource;

    public AdminController(NotificacionService notificacionService,EntrenadorService entrenadorService, EquipoService equipoService, PdfService pdfService ,LiquidacionService liquidacionService, ArbitroService arbitroService, PartidoService partidoService, EscalafonRepository escalafonRepository) {
        this.arbitroService = arbitroService;
        this.partidoService = partidoService;
        this.liquidacionService = liquidacionService;
        this.pdfGeneratorService = pdfService;
        this.equipoService = equipoService;
        this.entrenadorService = entrenadorService;
        this.escalafonRepository = escalafonRepository;
        this.notificacionService = notificacionService;
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
    public String arbitros(Model model, Locale locale) {
        model.addAttribute("arbitros", arbitroService.findAll());
        return "admin/arbitros";
    }
   
    @GetMapping("/notificaciones")
    public String notificaciones(Model model, Locale locale){
        model.addAttribute("notificaciones", notificacionService.obtenerNotificacionesAdmin());
        return "admin/notificaciones";
    }

    @GetMapping("/liquidaciones")
    public String listarLiquidaciones(String periodo, Model model, Locale locale) {

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
    public String generarLiquidaciones(@RequestParam String periodo, RedirectAttributes redirectAttributes, Locale locale) {
        YearMonth ym = YearMonth.parse(periodo); // formato "YYYY-MM"
        liquidacionService.generarLiquidacionesMensuales(ym);
        String successMsg = messageSource.getMessage("msg.success.liquidaciones.generated", null, locale);
        redirectAttributes.addFlashAttribute("successMessage", successMsg);
        return "redirect:/admin/liquidaciones?periodo=" + ym;
    }


    @PostMapping("/liquidaciones/{id}/pagar")
    public String pagar(@PathVariable Long id, RedirectAttributes redirectAttributes, Locale locale) {
        Optional<Liquidacion> opt = liquidacionService.findById(id);
        if (opt.isPresent() && opt.get().getEstado() == Liquidacion.EstadoLiquidacion.PENDIENTE) {
            liquidacionService.marcarComoPagada(id);
            String successMsg = messageSource.getMessage("msg.success.liquidacion.paid", null, locale);
            redirectAttributes.addFlashAttribute("successMessage", successMsg);
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
    public String createArbitro(Model model, Locale locale) {
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
                            RedirectAttributes redirectAttributes,
                            Locale locale) {
        try {
            if (result.hasErrors()) {
                model.addAttribute("escalafones", escalafonRepository.findAll());
                return "admin/createArbitro";
            }

            arbitroService.createArbitroWithPhoto(arbitro, photoFile);
            String successMsg = messageSource.getMessage("msg.success.arbitro.created", null, locale);
            redirectAttributes.addFlashAttribute("successMessage", successMsg);
            return "redirect:/admin/arbitros";
            
        } catch (IllegalArgumentException e) {
            String errorMsg = messageSource.getMessage("msg.error.duplicate", null, locale);
            model.addAttribute("errorMessage", errorMsg + ": " + e.getMessage());
            model.addAttribute("escalafones", escalafonRepository.findAll());
            return "admin/createArbitro";
        } catch (Exception e) {
            String errorMsg = messageSource.getMessage("msg.error.save", null, locale);
            model.addAttribute("errorMessage", errorMsg);
            model.addAttribute("escalafones", escalafonRepository.findAll());
            return "admin/createArbitro";
        }
    }

    @GetMapping("/arbitros/edit/{id}")
    public String editArbitro(@PathVariable Long id, Model model, Locale locale) {
        Arbitro arbitro = arbitroService.findById(id)
            .orElseThrow(() -> new IllegalArgumentException(
                messageSource.getMessage("msg.error.arbitro.notfound", null, locale)));
        // Asegura objeto usuario para el form, si lo usas ahí
        if (arbitro.getUsuario() == null) {
            arbitro.setUsuario(new Usuario());
        }
        model.addAttribute("arbitro", arbitro);
        model.addAttribute("escalafones", escalafonRepository.findAll());
        model.addAttribute("modoEdicion", true);
        return "admin/createArbitro";
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
                               Model model, 
                               RedirectAttributes redirectAttributes,
                               Locale locale) {

        if (result.hasErrors()) {
            String errorMsg = messageSource.getMessage("msg.error.form.validation", null, locale);
            model.addAttribute("errorMessage", errorMsg);
            model.addAttribute("escalafones", escalafonRepository.findAll());
            model.addAttribute("modoEdicion", true);
            return "admin/arbitro_form";
        }

        try {
            // Set the ID to ensure we're updating the correct arbitro
            arbitro.setId(id);
            
            Arbitro updatedArbitro = arbitroService.updateArbitroWithPhoto(arbitro, photoFile, removePhoto, updatePassword);

            String successMsg = messageSource.getMessage("msg.success.arbitro.updated", 
                new Object[]{updatedArbitro.getNombre()}, locale);
            redirectAttributes.addFlashAttribute("successMessage", successMsg);

            return "redirect:/admin/arbitros";
            
        } catch (RuntimeException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("escalafones", escalafonRepository.findAll());
            model.addAttribute("modoEdicion", true);
            return "admin/arbitro_form";
        } catch (Exception e) {
            System.err.println("Error al actualizar árbitro: " + e.getMessage());
            e.printStackTrace();
            
            String errorMsg = messageSource.getMessage("msg.error.update", null, locale);
            model.addAttribute("errorMessage", errorMsg);
            model.addAttribute("escalafones", escalafonRepository.findAll());
            model.addAttribute("modoEdicion", true);
            return "admin/arbitro_form";
        }
    }

    @PostMapping("/arbitros/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes, Locale locale) {
        try {
            boolean deleted = arbitroService.deleteById(id);
            if (deleted) {
                String successMsg = messageSource.getMessage("msg.success.arbitro.deleted", null, locale);
                redirectAttributes.addFlashAttribute("successMessage", successMsg);
            } else {
                String errorMsg = messageSource.getMessage("msg.error.notfound", null, locale);
                redirectAttributes.addFlashAttribute("errorMessage", errorMsg);
            }
        } catch (DataIntegrityViolationException e) {
            String errorMsg = messageSource.getMessage("msg.error.delete", null, locale);
            redirectAttributes.addFlashAttribute("errorMessage", errorMsg);
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            String errorMsg = messageSource.getMessage("msg.error.general", null, locale);
            redirectAttributes.addFlashAttribute("errorMessage", errorMsg);
            System.err.println("Error al eliminar árbitro con ID " + id + ": " + e.getMessage());
        }
        return "redirect:/admin/arbitros";
    }

  
    @GetMapping("/arbitros/{id}")
    public String viewArbitro(@PathVariable Long id, Model model, Locale locale) {
        Arbitro arbitro = arbitroService.findById(id)
            .orElseThrow(() -> new IllegalArgumentException(
                messageSource.getMessage("msg.error.arbitro.notfound", null, locale)));
        model.addAttribute("arbitro", arbitro);
        return "admin/arbitro_view"; // ver plantilla en el punto 4
    }
  
    // ==================== COACHES/ENTRENADORES ====================


    @GetMapping("/coaches")
    public String listarCoaches(Model model, Locale locale) {
        List<Entrenador> coaches = entrenadorService.findAllActive();
        model.addAttribute("coaches", coaches);
        return "admin/coaches";
    }

    /**
     * Mostrar formulario para crear un nuevo coach
     * Ruta: /admin/coaches/create
     */
    @GetMapping("/coaches/create")
    public String mostrarFormularioCrearCoach(Model model, Locale locale) {
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
            RedirectAttributes redirectAttributes,
            Locale locale) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("categorias", Entrenador.Categoria.values());
            String errorMsg = messageSource.getMessage("msg.error.form.validation", null, locale);
            model.addAttribute("errorMessage", errorMsg);
            return "admin/coaches_form";
        }

        try {
            // Validar que no exista un coach con la misma cédula
            if (entrenadorService.existsByCedula(entrenador.getCedula())) {
                String errorMsg = messageSource.getMessage("msg.error.duplicate.cedula", null, locale);
                model.addAttribute("errorMessage", errorMsg);
                model.addAttribute("categorias", Entrenador.Categoria.values());
                return "admin/coaches_form";
            }

            // Validar que no exista un coach con el mismo email
            if (entrenadorService.existsByEmail(entrenador.getEmail())) {
                String errorMsg = messageSource.getMessage("msg.error.duplicate.email", null, locale);
                model.addAttribute("errorMessage", errorMsg);
                model.addAttribute("categorias", Entrenador.Categoria.values());
                return "admin/coaches_form";
            }

            // Crear el coach con usuario y contraseña por defecto
            Entrenador savedCoach = entrenadorService.createCoachWithUser(entrenador);

            String successMsg = messageSource.getMessage("msg.success.coach.created", 
                new Object[]{savedCoach.getNombreCompleto()}, locale);
            redirectAttributes.addFlashAttribute("successMessage", successMsg);

            return "redirect:/admin/coaches";

        } catch (RuntimeException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("categorias", Entrenador.Categoria.values());
            return "admin/coaches_form";
        } catch (Exception e) {
            System.err.println("Error al crear coach: " + e.getMessage());
            e.printStackTrace();
            
            String errorMsg = messageSource.getMessage("msg.error.save", null, locale);
            model.addAttribute("errorMessage", errorMsg);
            model.addAttribute("categorias", Entrenador.Categoria.values());
            return "admin/coaches_form";
        }
    }

    /**
     * Ver detalles completos de un coach
     * Ruta: /admin/coaches/view/{id}
     */
    @GetMapping("/coaches/view/{id}")
    public String verCoach(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes, Locale locale) {
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
            String errorMsg = messageSource.getMessage("msg.error.coach.notfound", null, locale);
            redirectAttributes.addFlashAttribute("errorMessage", errorMsg);
            return "redirect:/admin/coaches";
        }
    }

    /**
     * Mostrar formulario para editar un coach
     * Ruta: /admin/coaches/edit/{id}
     */
    @GetMapping("/coaches/edit/{id}")
    public String mostrarFormularioEditarCoach(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes, Locale locale) {
        Optional<Entrenador> coachOpt = entrenadorService.findById(id);
        
        if (coachOpt.isPresent()) {
            model.addAttribute("entrenador", coachOpt.get());
            model.addAttribute("categorias", Entrenador.Categoria.values());
            model.addAttribute("editMode", true);
            return "admin/coaches_form";
        } else {
            String errorMsg = messageSource.getMessage("msg.error.coach.notfound", null, locale);
            redirectAttributes.addFlashAttribute("errorMessage", errorMsg);
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
            RedirectAttributes redirectAttributes,
            Locale locale) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("categorias", Entrenador.Categoria.values());
            model.addAttribute("editMode", true);
            String errorMsg = messageSource.getMessage("msg.error.form.validation", null, locale);
            model.addAttribute("errorMessage", errorMsg);
            return "admin/coaches_form";
        }

        try {
            Optional<Entrenador> coachOpt = entrenadorService.findById(id);
            
            if (coachOpt.isEmpty()) {
                String errorMsg = messageSource.getMessage("msg.error.coach.notfound", null, locale);
                redirectAttributes.addFlashAttribute("errorMessage", errorMsg);
                return "redirect:/admin/coaches";
            }

            Entrenador coachExistente = coachOpt.get();
            
            // Validar cédula única (excluyendo el coach actual)
            if (!coachExistente.getCedula().equals(entrenadorActualizado.getCedula()) &&
                entrenadorService.existsByCedula(entrenadorActualizado.getCedula())) {
                String errorMsg = messageSource.getMessage("msg.error.duplicate.cedula", null, locale);
                model.addAttribute("errorMessage", errorMsg);
                model.addAttribute("categorias", Entrenador.Categoria.values());
                model.addAttribute("editMode", true);
                return "admin/coaches_form";
            }

            // Validar email único (excluyendo el coach actual)
            if (!coachExistente.getEmail().equals(entrenadorActualizado.getEmail()) &&
                entrenadorService.existsByEmail(entrenadorActualizado.getEmail())) {
                String errorMsg = messageSource.getMessage("msg.error.duplicate.email", null, locale);
                model.addAttribute("errorMessage", errorMsg);
                model.addAttribute("categorias", Entrenador.Categoria.values());
                model.addAttribute("editMode", true);
                return "admin/coaches_form";
            }

            // Actualizar los campos del coach
            entrenadorActualizado.setId(id);
            entrenadorService.updateCoach(entrenadorActualizado);

            String successMsg = messageSource.getMessage("msg.success.coach.updated", 
                new Object[]{entrenadorActualizado.getNombreCompleto()}, locale);
            redirectAttributes.addFlashAttribute("successMessage", successMsg);

            return "redirect:/admin/coaches/view/" + id;

        } catch (RuntimeException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("categorias", Entrenador.Categoria.values());
            model.addAttribute("editMode", true);
            return "admin/coaches_form";
        } catch (Exception e) {
            String errorMsg = messageSource.getMessage("msg.error.update", null, locale);
            redirectAttributes.addFlashAttribute("errorMessage", errorMsg);
            return "redirect:/admin/coaches";
        }
    }

    /**
     * Eliminar un coach
     * Ruta: POST /admin/coaches/delete/{id}
     */
    @PostMapping("/coaches/delete/{id}")
    public String eliminarCoach(@PathVariable Long id, RedirectAttributes redirectAttributes, Locale locale) {
        try {
            Optional<Entrenador> coachOpt = entrenadorService.findById(id);
            String coachName = coachOpt.map(Entrenador::getNombreCompleto).orElse(null);
            
            boolean deleted = entrenadorService.deleteById(id);
            
            if (deleted) {
                String successMsg = messageSource.getMessage("msg.success.coach.deleted", 
                    new Object[]{coachName != null ? coachName : ""}, locale);
                redirectAttributes.addFlashAttribute("successMessage", successMsg);
            } else {
                String errorMsg = messageSource.getMessage("msg.error.notfound", null, locale);
                redirectAttributes.addFlashAttribute("errorMessage", errorMsg);
            }
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            String errorMsg = messageSource.getMessage("msg.error.general", null, locale);
            redirectAttributes.addFlashAttribute("errorMessage", errorMsg);
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
    public String partidos(Model model, Locale locale) {
        List<Partido> partidos = partidoService.findAll();
        model.addAttribute("partidos", partidos);
        return "admin/partidos";
    }

    @GetMapping("/lop")
    public ResponseEntity<Map<String, Object>> show(Model model, Locale locale) {
        //Map<String, Object> response = new HashMap<>();

        Optional<Partido> partidos = partidoService.findById(1L);
        Map<String, Object> arbri = new HashMap<>();
        arbri.put("partidos", partidos.get());
        return ResponseEntity.ok(arbri);
    }

    @GetMapping("/partidos/create")
    public String mostrarFormularioCrear(Model model, Locale locale) {
        Partido p = new Partido();
        // Establecer el estado por defecto
        p.setEstado(Partido.EstadoPartido.PENDIENTE_CONFIRMACION);
        // Evita null al evaluar *{arbitro.id} y *{equipo*.id} en la vista
        p.setArbitro(new Arbitro());
        p.setEquipoLocal(new Equipo());
        p.setEquipoVisitante(new Equipo());

        model.addAttribute("partido", p);
        // Asegurar que siempre hay listas no nulas
        List<Arbitro> arbitros = arbitroService.findAll();
        List<Equipo> equipos = equipoService.findAll();
        model.addAttribute("arbitros", arbitros != null ? arbitros : new java.util.ArrayList<>());
        model.addAttribute("equipos", equipos != null ? equipos : new java.util.ArrayList<>());
        model.addAttribute("estados", Partido.EstadoPartido.values());

        return "admin/partido_form";
    }
    @PostMapping("/partidos/save")
    public String guardar(@ModelAttribute Partido partido, Model model, RedirectAttributes redirectAttributes, Locale locale) {
        try {
            // Validar que los equipos sean diferentes
            if (partido.getEquipoLocal() != null && partido.getEquipoVisitante() != null &&
                partido.getEquipoLocal().getId().equals(partido.getEquipoVisitante().getId())) {
                String errorMsg = messageSource.getMessage("msg.error.partido.sameTeam", null, locale);
                model.addAttribute("errorMessage", errorMsg);
                model.addAttribute("partido", partido);
                model.addAttribute("arbitros", arbitroService.findAll());
                model.addAttribute("equipos", equipoService.findAll());
                model.addAttribute("estados", Partido.EstadoPartido.values());
                return "admin/partido_form";
            }
            
            if (partido.getArbitro() != null && partido.getArbitro().getId() != null) {
                arbitroService.findById(partido.getArbitro().getId())
                            .ifPresent(partido::setArbitro);
            } else {
                partido.setArbitro(null);
            }
            
            partidoService.crearPartido(partido);
            String successMsg = messageSource.getMessage("msg.success.partido.created", null, locale);
            redirectAttributes.addFlashAttribute("successMessage", successMsg);
            return "redirect:/admin/partidos";
            
        } catch (Exception e) {
            String errorMsg = messageSource.getMessage("msg.error.save", null, locale);
            model.addAttribute("errorMessage", errorMsg + ": " + e.getMessage());
            model.addAttribute("partido", partido);
            model.addAttribute("arbitros", arbitroService.findAll());
            model.addAttribute("equipos", equipoService.findAll());
            model.addAttribute("estados", Partido.EstadoPartido.values());
            return "admin/partido_form";
        }

    }




    @GetMapping("/partidos/edit/{id}")
    public String editar(@PathVariable Long id, Model model, Locale locale) {
        Partido partido = partidoService.findById(id)
            .orElseThrow(() -> new IllegalArgumentException(
                messageSource.getMessage("msg.error.partido.notfound", null, locale)));
        model.addAttribute("partido", partido);
        // Asegurar que siempre hay listas no nulas
        List<Arbitro> arbitros = arbitroService.findAll();
        List<Equipo> equipos = equipoService.findAll();
        model.addAttribute("arbitros", arbitros != null ? arbitros : new java.util.ArrayList<>());
        model.addAttribute("equipos", equipos != null ? equipos : new java.util.ArrayList<>());
        model.addAttribute("estados", Partido.EstadoPartido.values());
        return "admin/partido_form";
    }

    @PostMapping("/partidos/delete/{id}")
    public String deletePartido(@PathVariable Long id, RedirectAttributes ra, Locale locale) {
        try {
            Optional<Partido> opt = partidoService.findById(id);
            String label = opt.map(p -> p.getEquipoLocal().getNombre() + " vs " + p.getEquipoVisitante().getNombre())
                            .orElse("Partido #" + id);

            boolean deleted = partidoService.deleteById(id);

            if (deleted) {
                String successMsg = messageSource.getMessage("msg.success.partido.deleted", 
                    new Object[]{label}, locale);
                ra.addFlashAttribute("successMessage", successMsg);
            } else {
                String errorMsg = messageSource.getMessage("msg.error.notfound", null, locale);
                ra.addFlashAttribute("errorMessage", errorMsg);
            }
        } catch (RuntimeException ex) {
            ra.addFlashAttribute("errorMessage", ex.getMessage());
        } catch (Exception ex) {
            String errorMsg = messageSource.getMessage("msg.error.general", null, locale);
            ra.addFlashAttribute("errorMessage", errorMsg);
            System.err.println("Error al eliminar partido " + id + ": " + ex.getMessage());
        }
        return "redirect:/admin/partidos";
    }

    @PostMapping("/partidos/update/{id}")
    public String actualizar(@PathVariable Long id, @ModelAttribute Partido partido, Model model, RedirectAttributes redirectAttributes, Locale locale) {
        try {
            // Validar que los equipos sean diferentes
            if (partido.getEquipoLocal() != null && partido.getEquipoVisitante() != null &&
                partido.getEquipoLocal().getId().equals(partido.getEquipoVisitante().getId())) {
                String errorMsg = messageSource.getMessage("msg.error.partido.sameTeam", null, locale);
                model.addAttribute("errorMessage", errorMsg);
                model.addAttribute("partido", partido);
                model.addAttribute("arbitros", arbitroService.findAll());
                model.addAttribute("equipos", equipoService.findAll());
                model.addAttribute("estados", Partido.EstadoPartido.values());
                return "admin/partido_form";
            }
            
            // Verificar que el partido existe
            if (!partidoService.findById(id).isPresent()) {
                String errorMsg = messageSource.getMessage("msg.error.partido.notfound", null, locale);
                redirectAttributes.addFlashAttribute("errorMessage", errorMsg);
                return "redirect:/admin/partidos";
            }
            
            // Establecer el ID para la actualización
            partido.setId(id);
            
            if (partido.getArbitro() != null && partido.getArbitro().getId() != null) {
                arbitroService.findById(partido.getArbitro().getId())
                            .ifPresent(partido::setArbitro);
            } else {
                partido.setArbitro(null);
            }
            
            partidoService.crearPartido(partido); // Reutilizar crearPartido que debería manejar tanto crear como actualizar
            String successMsg = messageSource.getMessage("msg.success.partido.updated", null, locale);
            redirectAttributes.addFlashAttribute("successMessage", successMsg);
            return "redirect:/admin/partidos";
            
        } catch (Exception e) {
            String errorMsg = messageSource.getMessage("msg.error.update", null, locale);
            model.addAttribute("errorMessage", errorMsg + ": " + e.getMessage());
            model.addAttribute("partido", partido);
            model.addAttribute("arbitros", arbitroService.findAll());
            model.addAttribute("equipos", equipoService.findAll());
            model.addAttribute("estados", Partido.EstadoPartido.values());
            return "admin/partido_form";
        }

    }
}