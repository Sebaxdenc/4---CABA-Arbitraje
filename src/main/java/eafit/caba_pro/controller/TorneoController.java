package eafit.caba_pro.controller;

import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import eafit.caba_pro.model.Torneo;
import eafit.caba_pro.service.TorneoService;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/admin/torneos")
public class TorneoController {

    private final TorneoService torneoService;

    public TorneoController(TorneoService torneoService) {
        this.torneoService = torneoService;
    }

    @GetMapping
    public String index(Model model) {
        var torneos = torneoService.findAll();
        var conteos = new java.util.HashMap<Long, Long>();
        for (var t : torneos) {
            conteos.put(t.getId(), torneoService.contarPartidosDeTorneo(t.getId()));
        }
        model.addAttribute("torneos", torneos);
        model.addAttribute("conteos", conteos); // úsalo en la vista
        return "admin/torneos/index";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("torneo", new Torneo());
        model.addAttribute("partidosSinTorneo", torneoService.listarPartidosSinTorneoProgramados());
        return "admin/torneos/form";
    }

    @PostMapping("/guardar")
    public String guardar(@Valid @ModelAttribute("torneo") Torneo torneo,
                          BindingResult br,
                          @RequestParam(name = "partidoIds", required = false) List<Long> partidoIds,
                          Model model,
                          RedirectAttributes ra) {
        if (br.hasErrors()) {
            model.addAttribute("partidosSinTorneo", torneoService.listarPartidosSinTorneoProgramados());
            return "admin/torneos/form";
        }
        try {
            torneoService.crearConPartidos(torneo, partidoIds);
            ra.addFlashAttribute("ok", "Torneo creado.");
            return "redirect:/admin/torneos";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            model.addAttribute("partidosSinTorneo", torneoService.listarPartidosSinTorneoProgramados());
            return "admin/torneos/form";
        }
    }

    @GetMapping("/{id:[0-9]+}/editar")
    public String editar(@PathVariable Long id, Model model) {
        Torneo t = torneoService.findById(id).orElseThrow();
        model.addAttribute("torneo", t);
        return "admin/torneos/form";
    }

    @PostMapping("/{id:[0-9]+}/actualizar")
    public String actualizar(@PathVariable Long id,
                             @Valid @ModelAttribute("torneo") Torneo torneo,
                             BindingResult br,
                             Model model,
                             RedirectAttributes ra) {
        if (br.hasErrors()) {
            return "admin/torneos/form";
        }
        torneoService.update(id, torneo);
        ra.addFlashAttribute("ok", "Torneo actualizado.");
        return "redirect:/admin/torneos";
    }

    @GetMapping("/{id:[0-9]+}")
    public String detalle(@PathVariable Long id, Model model, RedirectAttributes ra) {
        var opt = torneoService.findById(id);
        if (opt.isEmpty()) {
            ra.addFlashAttribute("errorMessage", "Torneo no encontrado.");
            return "redirect:/admin/torneos";
        }

        var torneo = opt.get();
        model.addAttribute("torneo", torneo);
        model.addAttribute("partidosAsignados", torneoService.listarPartidosDeTorneo(id));
        model.addAttribute("partidosSinTorneo", torneoService.listarPartidosSinTorneoProgramados());

        // formateo de fechas para NO usar #temporals en la vista
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String fi = (torneo.getFechaInicio() != null) ? torneo.getFechaInicio().format(fmt) : "-";
        String ff = (torneo.getFechaFin() != null) ? torneo.getFechaFin().format(fmt) : "-";
        model.addAttribute("fechaInicioFmt", fi);
        model.addAttribute("fechaFinFmt", ff);

        return "admin/torneos/detalle";
    }

    @PostMapping("/{id:[0-9]+}/asignar-partido")
    public String asignar(@PathVariable Long id,
                          @RequestParam Long partidoId,
                          RedirectAttributes ra) {
        try {
            torneoService.asignarPartido(id, partidoId);
            ra.addFlashAttribute("ok", "Partido asignado.");
        } catch (IllegalArgumentException ex) {
            ra.addFlashAttribute("ok", null);
            ra.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/torneos/{id}";
    }

    @PostMapping("/{id:[0-9]+}/quitar-partido")
    public String quitar(@PathVariable Long id,
                         @RequestParam Long partidoId,
                         RedirectAttributes ra) {
        try {
            torneoService.quitarPartido(id, partidoId);
            ra.addFlashAttribute("ok", "Partido quitado.");
        } catch (IllegalArgumentException ex) {
            ra.addFlashAttribute("ok", null);
            ra.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/torneos/{id}";
    }

    @PostMapping("/{id:[0-9]+}/eliminar")
    public String eliminar(@PathVariable Long id, RedirectAttributes ra) {
        try {
          
            String nombre = torneoService.findById(id).map(Torneo::getNombre).orElse("Torneo #" + id);

            torneoService.delete(id);


            ra.addFlashAttribute("ok", "¡Torneo '" + nombre + "' eliminado.");
        } catch (IllegalArgumentException ex) {
            ra.addFlashAttribute("errorMessage", ex.getMessage());
        } catch (DataIntegrityViolationException ex) {
            ra.addFlashAttribute("errorMessage",
                "No se pudo eliminar por restricciones de base de datos.");
        } catch (Exception ex) {
            ra.addFlashAttribute("errorMessage",
                "Error interno al eliminar el torneo. Intenta nuevamente.");
        }
        return "redirect:/admin/torneos";
    }

    // 0) Verifica que el mapping se alcanza
    @GetMapping("/{id:[0-9]+}/ping")
    public @ResponseBody String ping(@PathVariable Long id) {
        return "PING /admin/torneos/" + id;
    }

    // 1) Verifica que el post de eliminar llega
    @PostMapping("/{id:[0-9]+}/eliminar-ping")
    public @ResponseBody String eliminarPing(@PathVariable Long id) {
        return "ELIMINAR PING torneo=" + id;
    }

}
