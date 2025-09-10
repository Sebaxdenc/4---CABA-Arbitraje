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
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
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
    
    @GetMapping
    public String panelCoach(Model model, Principal principal, RedirectAttributes redirectAttributes) {
        try {
            // Obtener el usuario autenticado
            String username = principal.getName();
            
            // Buscar el entrenador asociado a este usuario
            Optional<Entrenador> entrenadorOpt = entrenadorService.findByUsuarioUsername(username);
            
            if (entrenadorOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "No se encontró información del entrenador para este usuario");
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
            redirectAttributes.addFlashAttribute("error", "Error al cargar el panel del entrenador");
            return "redirect:/login";
        }
    }
    
    /**
     * Ver partidos del equipo del coach
     * Ruta: /coach/partidos
     */
    @GetMapping("/partidos")
    public String verPartidos(Model model, Principal principal, RedirectAttributes redirectAttributes) {
        try {
            String username = principal.getName();
            Optional<Entrenador> entrenadorOpt = entrenadorService.findByUsuarioUsername(username);
            
            if (entrenadorOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "No se encontró información del entrenador");
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
            redirectAttributes.addFlashAttribute("error", "Error al cargar los partidos");
            return "redirect:/coach";
        }
    }
    
    /**
     * Formulario para crear una nueva reseña
     * Ruta: /coach/crear-reseña
     */
    @GetMapping("/crear-reseña")
    public String mostrarFormularioCrearReseña(Model model, Principal principal, RedirectAttributes redirectAttributes) {
        try {
            String username = principal.getName();
            Optional<Entrenador> entrenadorOpt = entrenadorService.findByUsuarioUsername(username);
            
            if (entrenadorOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "No se encontró información del entrenador");
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
            redirectAttributes.addFlashAttribute("error", "Error al cargar el formulario de reseña");
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
            RedirectAttributes redirectAttributes) {
        
        try {
            String username = principal.getName();
            Optional<Entrenador> entrenadorOpt = entrenadorService.findByUsuarioUsername(username);
            
            if (entrenadorOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "No se encontró información del entrenador");
                return "redirect:/coach";
            }
            
            Entrenador entrenador = entrenadorOpt.get();
            
            // Validar que el árbitro existe
            Optional<Arbitro> arbitroOpt = arbitroService.findById(arbitroId);
            if (arbitroOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "El árbitro seleccionado no existe");
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
                        redirectAttributes.addFlashAttribute("error", "No puedes crear reseñas para partidos que no involucran a tu equipo");
                        return "redirect:/coach/crear-reseña";
                    }
                    
                    // Verificar que no se haya creado una reseña previa para este partido y árbitro
                    var reseñasExistentes = reseñaService.findByArbitroAndPartidoAndEntrenador(arbitroOpt.get(), partido, entrenador.getId());
                    if (!reseñasExistentes.isEmpty()) {
                        redirectAttributes.addFlashAttribute("error", "Ya has creado una reseña para este árbitro en este partido");
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
            
            redirectAttributes.addFlashAttribute("mensaje", "Reseña creada exitosamente");
            redirectAttributes.addFlashAttribute("tipoMensaje", "success");
            
            return "redirect:/coach";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al crear la reseña: " + e.getMessage());
            return "redirect:/coach/crear-reseña";
        }
    }

    @GetMapping("/reseñas")
    public String verReseñas(Model model, Principal principal, RedirectAttributes redirectAttributes) {
        
        try {
            String username = principal.getName();
            Optional<Entrenador> entrenadorOpt = entrenadorService.findByUsuarioUsername(username);
            
            if (entrenadorOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "No se encontró información del entrenador");
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
            redirectAttributes.addFlashAttribute("error", "Error al cargar las reseñas");
            return "redirect:/coach";
        }
    }
    

    @GetMapping("/perfil")
    public String verPerfil(Model model, Principal principal, RedirectAttributes redirectAttributes) {
        try {
            String username = principal.getName();
            Optional<Entrenador> entrenadorOpt = entrenadorService.findByUsuarioUsername(username);
            
            if (entrenadorOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "No se encontró información del entrenador");
                return "redirect:/coach";
            }
            
            model.addAttribute("entrenador", entrenadorOpt.get());
            model.addAttribute("categorias", Entrenador.Categoria.values());
            
            return "coach/perfil";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al cargar el perfil");
            return "redirect:/coach";
        }
    }
    

    @PostMapping("/perfil")
    public String actualizarPerfil(
            @ModelAttribute("entrenador") Entrenador entrenadorActualizado,
            Model model,
            Principal principal,
            RedirectAttributes redirectAttributes) {
        
        try {
            String username = principal.getName();
            Optional<Entrenador> entrenadorOpt = entrenadorService.findByUsuarioUsername(username);
            
            if (entrenadorOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "No se encontró información del entrenador");
                return "redirect:/coach";
            }
            
            Entrenador entrenadorExistente = entrenadorOpt.get();
            
            // Actualizar solo campos permitidos para el coach
            entrenadorExistente.setTelefono(entrenadorActualizado.getTelefono());
            entrenadorExistente.setEmail(entrenadorActualizado.getEmail());
            entrenadorExistente.setExperiencia(entrenadorActualizado.getExperiencia());
            entrenadorExistente.setEspecialidades(entrenadorActualizado.getEspecialidades());
            
            entrenadorService.save(entrenadorExistente);
            
            redirectAttributes.addFlashAttribute("mensaje", "Perfil actualizado correctamente");
            redirectAttributes.addFlashAttribute("tipoMensaje", "success");
            
            return "redirect:/coach/perfil";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al actualizar el perfil");
            return "redirect:/coach/perfil";
        }
    }
    
    @GetMapping("/estadisticas")
    public String verEstadisticas(Model model, Principal principal, RedirectAttributes redirectAttributes) {
        try {
            String username = principal.getName();
            Optional<Entrenador> entrenadorOpt = entrenadorService.findByUsuarioUsername(username);
            
            if (entrenadorOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "No se encontró información del entrenador");
                return "redirect:/coach";
            }
            
            Entrenador entrenador = entrenadorOpt.get();
            String equipo = entrenador.getEquipo();
            
            // Obtener estadísticas detalladas
            var estadisticas = partidoService.getEstadisticasDetalladasByEquipo(equipo);
            var ultimosPartidos = partidoService.findUltimos5PartidosByEquipo(equipo);
            var proximosPartidos = partidoService.findProximos5PartidosByEquipo(equipo);
            
            model.addAttribute("entrenador", entrenador);
            model.addAttribute("estadisticas", estadisticas);
            model.addAttribute("ultimosPartidos", ultimosPartidos);
            model.addAttribute("proximosPartidos", proximosPartidos);
            
            return "coach/estadisticas";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al cargar las estadísticas");
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