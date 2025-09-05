package eafit.caba_pro.controller;
import eafit.caba_pro.service.UsuarioService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;


@Controller
@RequestMapping("/arbitro")
public class ArbitroController {
    @Autowired
    UsuarioService usuarioService;

    @GetMapping
    public String dashboard(){
        //return "dsa";
        return "arbitro/dashboard";
    }

    // Ejemplo de como obtener el nombre de usuario de la actual sesión
    @GetMapping("/prueba")
    public String ejemplo(Model model){
        String username = usuarioService.getCurrentUsername();
        model.addAttribute("username", username);

        return "ejemplo";
    }
}
