package eafit.caba_pro.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/arbitro")
public class ArbitroController {
    @GetMapping
    public String dashboard(){
        return "arbitro/dashboard";
    }
}
