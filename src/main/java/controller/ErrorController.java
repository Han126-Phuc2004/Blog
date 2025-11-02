package controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/app-error")
public class ErrorController {

    @GetMapping
    public String handleError() {
        return "error"; // template error.html
    }
}

