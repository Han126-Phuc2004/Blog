package controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AboutController {
    @GetMapping("/about")
    public String about() {
        return "about"; // Tìm file about.html trong templates/
    }


    @GetMapping("/contact")
    public String contact() {
        return "contact"; // Tìm file about.html trong templates/
    }
}
