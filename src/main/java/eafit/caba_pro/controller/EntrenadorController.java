package eafit.caba_pro.controller;

import eafit.caba_pro.model.Entrenador;
import eafit.caba_pro.service.EntrenadorService;
import eafit.caba_pro.service.PartidoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.Optional;

@Controller
@RequestMapping("/coach")
public class EntrenadorController {
    
    @Autowired
    private EntrenadorService entrenadorService;
    
    @Autowired
    private PartidoService partidoService;
    
    /**
     * Panel principal del coach - Solo accesible para usuarios con rol COACH
     * Ruta: /coach
     */
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
     * Ver reseñas del coach
     * Ruta: /coach/reseñas
     */
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
    
    /**
     * Ver perfil del coach y permitir edición básica
     * Ruta: /coach/perfil
     */
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
    
    /**
     * Actualizar perfil del coach (información limitada)
     * Ruta: POST /coach/perfil
     */
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
    
    /**
     * Ver estadísticas detalladas del equipo
     * Ruta: /coach/estadisticas
     */
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
}