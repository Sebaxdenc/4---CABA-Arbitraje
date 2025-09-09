package eafit.caba_pro.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
    @GetMapping("/")
    public String landingPage() {

        return "home/landingpage"; // Return the name of the view (e.g., index.html)
    }

}
