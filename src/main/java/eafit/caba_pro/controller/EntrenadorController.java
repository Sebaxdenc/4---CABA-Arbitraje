package eafit.caba_pro.controller;

import eafit.caba_pro.model.Entrenador;
import eafit.caba_pro.service.EntrenadorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import java.util.Optional;

/**
 * Controller para gestionar las operaciones de Entrenador
 * Implementa las 6 actividades del Taller 01
 * Autor: [Tu nombre]
 * Fecha: [Fecha actual]
 * Rol: Desarrollador Backend - Módulo Entrenador
 */
@Controller
@RequestMapping("/entrenador")
public class EntrenadorController {
    
    @Autowired
    private EntrenadorService entrenadorService;
    
    /**
     * ACTIVIDAD 1: Vista inicial con 2 botones/enlaces
     * Ruta: /entrenador
     */
    @GetMapping
    public String vistaInicial(Model model) {
        return "entrenador/index";
    }
    
    /**
     * ACTIVIDAD 2: Formulario de creación
     * Ruta: /entrenador/crear
     */
    @GetMapping("/crear")
    public String mostrarFormularioCreacion(Model model) {
        model.addAttribute("entrenador", new Entrenador());
        model.addAttribute("categorias", Entrenador.Categoria.values());
        return "entrenador/crear";
    }
    
    /**
     * ACTIVIDAD 3: Inserción del objeto (procesar formulario)
     * Ruta: POST /entrenador/crear
     */
    @PostMapping("/crear")
    public String procesarFormularioCreacion(
            @Valid @ModelAttribute("entrenador") Entrenador entrenador,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {
        
        // Si hay errores de validación, regresar al formulario
        if (bindingResult.hasErrors()) {
            model.addAttribute("categorias", Entrenador.Categoria.values());
            return "entrenador/crear";
        }
        
        try {
            // Guardar el entrenador
            entrenadorService.save(entrenador);
            redirectAttributes.addFlashAttribute("mensaje", "Elemento creado satisfactoriamente");
            redirectAttributes.addFlashAttribute("tipoMensaje", "success");
            return "redirect:/entrenador/exito";
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("categorias", Entrenador.Categoria.values());
            return "entrenador/crear";
        }
    }
    
    /**
     * Página de éxito después de crear un entrenador
     */
    @GetMapping("/exito")
    public String paginaExito() {
        return "entrenador/exito";
    }
    
    /**
     * ACTIVIDAD 4: Listar objetos (solo ID y nombre identificativo)
     * Ruta: /entrenador/listar
     */
    @GetMapping("/listar")
    public String listarEntrenadores(Model model) {
        model.addAttribute("entrenadores", entrenadorService.findAllActive());
        return "entrenador/listar";
    }
    
    /**
     * ACTIVIDAD 5: Ver un objeto completo
     * Ruta: /entrenador/ver/{id}
     */
    @GetMapping("/ver/{id}")
    public String verEntrenador(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Entrenador> entrenadorOpt = entrenadorService.findById(id);
        
        if (entrenadorOpt.isPresent()) {
            model.addAttribute("entrenador", entrenadorOpt.get());
            return "entrenador/ver";
        } else {
            redirectAttributes.addFlashAttribute("error", "Entrenador no encontrado");
            return "redirect:/entrenador/listar";
        }
    }
    
    /**
     * ACTIVIDAD 6: Borrar objeto
     * Ruta: POST /entrenador/borrar/{id}
     */
    @PostMapping("/borrar/{id}")
    public String borrarEntrenador(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            entrenadorService.deleteById(id);
            redirectAttributes.addFlashAttribute("mensaje", "Entrenador eliminado correctamente");
            redirectAttributes.addFlashAttribute("tipoMensaje", "success");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", "Error al eliminar el entrenador: " + e.getMessage());
        }
        
        return "redirect:/entrenador/listar";
    }
    
    /**
     * Ruta adicional para editar entrenador (funcionalidad extra)
     */
    @GetMapping("/editar/{id}")
    public String mostrarFormularioEdicion(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Entrenador> entrenadorOpt = entrenadorService.findById(id);
        
        if (entrenadorOpt.isPresent()) {
            model.addAttribute("entrenador", entrenadorOpt.get());
            model.addAttribute("categorias", Entrenador.Categoria.values());
            return "entrenador/editar";
        } else {
            redirectAttributes.addFlashAttribute("error", "Entrenador no encontrado");
            return "redirect:/entrenador/listar";
        }
    }
    
    /**
     * Procesar edición de entrenador
     */
    @PostMapping("/editar/{id}")
    public String procesarEdicion(
            @PathVariable("id") Long id,
            @Valid @ModelAttribute("entrenador") Entrenador entrenador,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {
        
        if (bindingResult.hasErrors()) {
            model.addAttribute("categorias", Entrenador.Categoria.values());
            return "entrenador/editar";
        }
        
        try {
            entrenador.setId(id); // Asegurar que el ID esté correcto
            entrenadorService.save(entrenador);
            redirectAttributes.addFlashAttribute("mensaje", "Entrenador actualizado correctamente");
            redirectAttributes.addFlashAttribute("tipoMensaje", "success");
            return "redirect:/entrenador/ver/" + id;
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("categorias", Entrenador.Categoria.values());
            return "entrenador/editar";
        }
    }
    
    /**
     * Búsqueda de entrenadores (funcionalidad adicional)
     */
    @GetMapping("/buscar")
    public String buscarEntrenadores(
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) String equipo,
            @RequestParam(required = false) Entrenador.Categoria categoria,
            Model model) {
        
        if (nombre != null && !nombre.trim().isEmpty()) {
            model.addAttribute("entrenadores", entrenadorService.findByNombreCompleto(nombre));
            model.addAttribute("criterio", "nombre: " + nombre);
        } else if (equipo != null && !equipo.trim().isEmpty()) {
            model.addAttribute("entrenadores", entrenadorService.findByEquipo(equipo));
            model.addAttribute("criterio", "equipo: " + equipo);
        } else if (categoria != null) {
            model.addAttribute("entrenadores", entrenadorService.findByCategoria(categoria));
            model.addAttribute("criterio", "categoría: " + categoria.getDisplayName());
        } else {
            model.addAttribute("entrenadores", entrenadorService.findAllActive());
            model.addAttribute("criterio", "todos los entrenadores activos");
        }
        
        model.addAttribute("categorias", Entrenador.Categoria.values());
        return "entrenador/buscar";
    }
}