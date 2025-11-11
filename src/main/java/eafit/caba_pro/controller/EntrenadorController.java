package eafit.caba_pro.controller;

import eafit.caba_pro.model.Entrenador;
import eafit.caba_pro.model.Arbitro;
import eafit.caba_pro.model.Reseña;
import eafit.caba_pro.model.Partido;
import eafit.caba_pro.service.EntrenadorService;
import eafit.caba_pro.service.PartidoService;
import eafit.caba_pro.service.ArbitroService;
import eafit.caba_pro.service.ReseñaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/coach")
public class EntrenadorController {
    
    @Autowired
    private EntrenadorService entrenadorService;
    
    @Autowired
    private PartidoService partidoService;
    
    @Autowired
    private ArbitroService arbitroService;
    
    @Autowired
    private ReseñaService reseñaService;
    
    @Autowired
    private MessageSource messageSource;
    
    @GetMapping
    public String panelCoach(Model model, Principal principal, Locale locale) {
        try {
            // Obtener el usuario autenticado
            String username = principal.getName();
            
            // Buscar el entrenador asociado a este usuario
            Optional<Entrenador> entrenadorOpt = entrenadorService.findByUsuarioUsername(username);
            
            if (entrenadorOpt.isEmpty()) {
                return "redirect:/login";
            }
            
            Entrenador entrenador = entrenadorOpt.get();
            
            // Obtener estadísticas del coach
            int partidosJugados = partidoService.countPartidosByEquipo(entrenador.getEquipo());
            int partidosGanados = partidoService.countPartidosGanadosByEquipo(entrenador.getEquipo());
            int partidosPerdidos = partidoService.countPartidosPerdidosByEquipo(entrenador.getEquipo());
            int partidosEmpatados = partidoService.countPartidosEmpatadosByEquipo(entrenador.getEquipo());
            
            // Calcular porcentaje de victorias
            double porcentajeVictorias = partidosJugados > 0 ? (partidosGanados * 100.0 / partidosJugados) : 0;
            
            model.addAttribute("entrenador", entrenador);
            model.addAttribute("partidosJugados", partidosJugados);
            model.addAttribute("partidosGanados", partidosGanados);
            model.addAttribute("partidosPerdidos", partidosPerdidos);
            model.addAttribute("partidosEmpatados", partidosEmpatados);
            model.addAttribute("porcentajeVictorias", String.format("%.1f", porcentajeVictorias));
            
            return "coach/panel";
            
        } catch (Exception e) {
            return "redirect:/login";
        }
    }
    
    /**
     * Ver partidos del equipo del coach
     * Ruta: /coach/partidos
     */
    @GetMapping("/partidos")
    public String verPartidos(Model model, Principal principal, RedirectAttributes redirectAttributes, Locale locale) {
        try {
            String username = principal.getName();
            Optional<Entrenador> entrenadorOpt = entrenadorService.findByUsuarioUsername(username);
            
            if (entrenadorOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", messageSource.getMessage("msg.error.entrenador.no.encontrado", null, locale));
                return "redirect:/coach";
            }
            
            Entrenador entrenador = entrenadorOpt.get();
            
            // Obtener partidos del equipo
            var partidosProgramados = partidoService.findPartidosProgramadosByEquipo(entrenador.getEquipo());
            var partidosFinalizados = partidoService.findPartidosFinalizadosByEquipo(entrenador.getEquipo());
            
            model.addAttribute("entrenador", entrenador);
            model.addAttribute("partidosProgramados", partidosProgramados);
            model.addAttribute("partidosFinalizados", partidosFinalizados);
            
            return "coach/partidos";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", messageSource.getMessage("msg.error.cargar.partidos", null, locale));
            return "redirect:/coach";
        }
    }
    
    /**
     * Formulario para crear una nueva reseña
     * Ruta: /coach/crear-reseña
     */
    @GetMapping("/crear-reseña")
    public String mostrarFormularioCrearReseña(Model model, Principal principal, RedirectAttributes redirectAttributes, Locale locale) {
        try {
            String username = principal.getName();
            Optional<Entrenador> entrenadorOpt = entrenadorService.findByUsuarioUsername(username);
            
            if (entrenadorOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", messageSource.getMessage("msg.error.entrenador.no.encontrado", null, locale));
                return "redirect:/coach";
            }
            
            Entrenador entrenador = entrenadorOpt.get();
            
            // Obtener todos los árbitros activos
            var arbitros = arbitroService.findAllActivos();
            
            model.addAttribute("entrenador", entrenador);
            model.addAttribute("arbitros", arbitros);
            model.addAttribute("nuevaReseña", new Reseña());
            
            return "coach/crear-reseña";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", messageSource.getMessage("msg.error.cargar.formulario.resena", null, locale));
            return "redirect:/coach";
        }
    }

    /**
     * Procesar la creación de una nueva reseña
     * Ruta: POST /coach/crear-reseña
     */
    @PostMapping("/crear-reseña")
    public String crearReseña(
            @Valid @ModelAttribute("nuevaReseña") Reseña nuevaReseña,
            BindingResult result,
            @RequestParam("arbitroId") Long arbitroId,
            @RequestParam(value = "partidoId", required = false) Long partidoId,
            Model model,
            Principal principal,
            RedirectAttributes redirectAttributes,
            Locale locale) {
        
        try {
            String username = principal.getName();
            Optional<Entrenador> entrenadorOpt = entrenadorService.findByUsuarioUsername(username);
            
            if (entrenadorOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", messageSource.getMessage("msg.error.entrenador.no.encontrado", null, locale));
                return "redirect:/coach";
            }
            
            Entrenador entrenador = entrenadorOpt.get();
            
            // Validar que el árbitro existe
            Optional<Arbitro> arbitroOpt = arbitroService.findById(arbitroId);
            if (arbitroOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", messageSource.getMessage("msg.error.arbitro.no.existe", null, locale));
                return "redirect:/coach/crear-reseña";
            }
            
            // Validar si se seleccionó un partido (opcional)
            Partido partido = null;
            if (partidoId != null) {
                Optional<Partido> partidoOpt = partidoService.findById(partidoId);
                if (partidoOpt.isPresent()) {
                    partido = partidoOpt.get();
                    
                    // Verificar que el partido pertenece al equipo del entrenador
                    if (!partido.getEquipoLocal().getNombre().equals(entrenador.getEquipo()) && 
                        !partido.getEquipoVisitante().getNombre().equals(entrenador.getEquipo())) {
                        redirectAttributes.addFlashAttribute("error", messageSource.getMessage("msg.error.partido.no.pertenece.equipo", null, locale));
                        return "redirect:/coach/crear-reseña";
                    }
                    
                    // Verificar que no se haya creado una reseña previa para este partido y árbitro
                    var reseñasExistentes = reseñaService.findByArbitroAndPartidoAndEntrenador(arbitroOpt.get(), partido, entrenador.getId());
                    if (!reseñasExistentes.isEmpty()) {
                        redirectAttributes.addFlashAttribute("error", messageSource.getMessage("msg.error.resena.ya.existe", null, locale));
                        return "redirect:/coach/crear-reseña";
                    }
                }
            }
            
            if (result.hasErrors()) {
                // Recargar datos para el formulario
                var arbitros = arbitroService.findAllActivos();
                
                model.addAttribute("entrenador", entrenador);
                model.addAttribute("arbitros", arbitros);
                
                return "coach/crear-reseña";
            }
            
            // Configurar la reseña
            nuevaReseña.setArbitro(arbitroOpt.get());
            nuevaReseña.setEntrenador(entrenador);
            nuevaReseña.setPartido(partido);
            
            // Guardar la reseña
            reseñaService.save(nuevaReseña);
            
            redirectAttributes.addFlashAttribute("mensaje", messageSource.getMessage("msg.success.resena.creada", null, locale));
            redirectAttributes.addFlashAttribute("tipoMensaje", "success");
            
            return "redirect:/coach";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", messageSource.getMessage("msg.error.crear.resena", null, locale));
            return "redirect:/coach/crear-reseña";
        }
    }

    @GetMapping("/reseñas")
    public String verReseñas(Model model, Principal principal, RedirectAttributes redirectAttributes, Locale locale) {
        
        try {
            String username = principal.getName();
            Optional<Entrenador> entrenadorOpt = entrenadorService.findByUsuarioUsername(username);
            
            if (entrenadorOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", messageSource.getMessage("msg.error.entrenador.no.encontrado", null, locale));
                return "redirect:/coach";
            }
            
            Entrenador entrenador = entrenadorOpt.get();
            
            // Obtener reseñas del entrenador
            var resenas = entrenadorService.findReseñasByEntrenador(entrenador.getId());
            
            
            // Calcular estadísticas de reseñas
            double promedioCalificacion = resenas.stream()
                .mapToDouble(r -> r.getPuntuacion())
                .average()
                .orElse(0.0);
            
            model.addAttribute("entrenador", entrenador);
            model.addAttribute("resenas", resenas);
            model.addAttribute("totalResenas", resenas.size());
            model.addAttribute("promedioCalificacion", String.format("%.1f", promedioCalificacion));
            
            return "coach/resenas";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", messageSource.getMessage("msg.error.cargar.resenas", null, locale));
            return "redirect:/coach";
        }
    }
    

    @GetMapping("/perfil")
    public String verPerfil(Model model, Principal principal, RedirectAttributes redirectAttributes, Locale locale) {
        try {
            String username = principal.getName();
            Optional<Entrenador> entrenadorOpt = entrenadorService.findByUsuarioUsername(username);
            
            if (entrenadorOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", messageSource.getMessage("msg.error.entrenador.no.encontrado", null, locale));
                return "redirect:/coach";
            }
            
            model.addAttribute("entrenador", entrenadorOpt.get());
            model.addAttribute("categorias", Entrenador.Categoria.values());
            
            return "coach/perfil";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", messageSource.getMessage("msg.error.cargar.perfil", null, locale));
            return "redirect:/coach";
        }
    }
    

    @PostMapping("/perfil")
    public String actualizarPerfil(
            @ModelAttribute("entrenador") Entrenador entrenadorActualizado,
            Model model,
            Principal principal,
            RedirectAttributes redirectAttributes,
            Locale locale) {
        
        try {
            String username = principal.getName();
            Optional<Entrenador> entrenadorOpt = entrenadorService.findByUsuarioUsername(username);
            
            if (entrenadorOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", messageSource.getMessage("msg.error.entrenador.no.encontrado", null, locale));
                return "redirect:/coach";
            }
            
            Entrenador entrenadorExistente = entrenadorOpt.get();
            
            // Actualizar solo campos permitidos para el coach
            entrenadorExistente.setTelefono(entrenadorActualizado.getTelefono());
            entrenadorExistente.setEmail(entrenadorActualizado.getEmail());
            entrenadorExistente.setExperiencia(entrenadorActualizado.getExperiencia());
            entrenadorExistente.setEspecialidades(entrenadorActualizado.getEspecialidades());
            
            entrenadorService.save(entrenadorExistente);
            
            redirectAttributes.addFlashAttribute("mensaje", messageSource.getMessage("msg.success.perfil.actualizado", null, locale));
            redirectAttributes.addFlashAttribute("tipoMensaje", "success");
            
            return "redirect:/coach/perfil";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", messageSource.getMessage("msg.error.actualizar.perfil", null, locale));
            return "redirect:/coach/perfil";
        }
    }
    
    @GetMapping("/estadisticas")
    public String obtenerEstadisticas(Model model, Principal principal, RedirectAttributes redirectAttributes, Locale locale) {
        try {
            String username = principal.getName();
            Optional<Entrenador> entrenadorOpt = entrenadorService.findByUsuarioUsername(username);
            
            if (entrenadorOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", messageSource.getMessage("msg.error.entrenador.no.encontrado", null, locale));
                return "redirect:/coach";
            }
            
            Entrenador entrenador = entrenadorOpt.get();
            var equipo = entrenador.getEquipoAsociado();
            
            Map<String, Object> estadisticas = new HashMap<>();
            estadisticas.put("entrenador", entrenador);
            estadisticas.put("equipo", equipo);
            estadisticas.put("equipoNombre", equipo != null ? equipo.getNombre() : "Sin equipo");
            estadisticas.put("equipoCiudad", equipo != null ? equipo.getCiudad() : "N/A");
            estadisticas.put("equipoFundacion", equipo != null ? equipo.getFundacion() : null);
            estadisticas.put("equipoEstado", equipo != null ? equipo.isEstado() : false);
            estadisticas.put("equipoLogo", equipo != null ? equipo.getLogo() : "");
            estadisticas.put("entrenadorNombre", entrenador.getNombreCompleto());
            estadisticas.put("categoriaEntrenador", entrenador.getCategoria().getDisplayName());
            estadisticas.put("experienciaEntrenador", entrenador.getExperiencia());
            
            // Agregar otras estadísticas
            estadisticas.put("totalPartidos", 0);
            estadisticas.put("partidosGanados", 0);
            estadisticas.put("partidosPerdidos", 0);
            estadisticas.put("porcentajeVictorias", 0.0);
            
            model.addAttribute("entrenador", entrenador);
            model.addAttribute("estadisticas", estadisticas);
            model.addAttribute("ultimosPartidos", new ArrayList<>());
            model.addAttribute("proximosPartidos", new ArrayList<>());
            
            return "coach/estadisticas";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", messageSource.getMessage("msg.error.cargar.estadisticas", null, locale));
            return "redirect:/coach";
        }
    }
    
    @GetMapping("/api/arbitro/{arbitroId}/partidos-finalizados")
    @ResponseBody
    public List<java.util.Map<String, Object>> getPartidosFinalizadosByArbitro(@PathVariable Long arbitroId, Principal principal) {
        try {
            // Obtener el entrenador en sesión
            String username = principal.getName();
            Optional<Entrenador> entrenadorOpt = entrenadorService.findByUsuarioUsername(username);
            
            if (entrenadorOpt.isEmpty()) {
                return new ArrayList<>();
            }
            
            String equipoEntrenador = entrenadorOpt.get().getEquipo();
            
            // Obtener partidos finalizados donde el árbitro arbitró Y el equipo del entrenador participó
            List<Partido> partidos = partidoService.findPartidosFinalizadosByArbitroYEquipo(arbitroId, equipoEntrenador);
            
            // Convertir a mapas simples para evitar problemas de serialización JSON
            return partidos.stream()
                    .map(partido -> {
                        java.util.Map<String, Object> partidoMap = new java.util.HashMap<>();
                        partidoMap.put("id", partido.getId());
                        partidoMap.put("fecha", partido.getFecha().toString());
                        partidoMap.put("hora", partido.getHora().toString());
                        partidoMap.put("estado", partido.getEstado().toString());
                        
                        // Información de equipos
                        java.util.Map<String, Object> equipoLocal = new java.util.HashMap<>();
                        equipoLocal.put("id", partido.getEquipoLocal().getId());
                        equipoLocal.put("nombre", partido.getEquipoLocal().getNombre());
                        
                        java.util.Map<String, Object> equipoVisitante = new java.util.HashMap<>();
                        equipoVisitante.put("id", partido.getEquipoVisitante().getId());
                        equipoVisitante.put("nombre", partido.getEquipoVisitante().getNombre());
                        
                        partidoMap.put("equipoLocal", equipoLocal);
                        partidoMap.put("equipoVisitante", equipoVisitante);
                        
                        return partidoMap;
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            // En caso de error, devolver lista vacía
            return new ArrayList<>();
        }
    }
}