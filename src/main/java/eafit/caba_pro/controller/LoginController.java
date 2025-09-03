package eafit.caba_pro.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import ch.qos.logback.core.model.Model;

@Controller
public class LoginController {
    @GetMapping("index")
    public String home(){
        return "Hola";
    }
}
