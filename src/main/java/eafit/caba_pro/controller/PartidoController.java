package eafit.caba_pro.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import eafit.caba_pro.model.Arbitro;
import eafit.caba_pro.model.Partido;
import eafit.caba_pro.service.ArbitroService;
import eafit.caba_pro.service.PartidoService;

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
