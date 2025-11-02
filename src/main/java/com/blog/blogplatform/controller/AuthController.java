package com.blog.blogplatform.controller;

import com.blog.blogplatform.entity.User;
import com.blog.blogplatform.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/")
    public String homePage() {
        return "home"; // home.html
    }



    @GetMapping("/post/1")
    public String post1Page() {
        return "post1"; // post1.html
    }

    @GetMapping("/post/2")
    public String post2Page() {
        return "post2"; // post2.html
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login"; // login.html
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("user", new User());
        return "register"; // register.html
    }

    @PostMapping("/register")
    public String registerUser(@RequestParam String username,
                               @RequestParam String password,
                               @RequestParam(required = false) String email,
                               @RequestParam(required = false) String fullName) {

        userService.registerUser(username, password, email, fullName);
        return "redirect:/login";
    }
}
