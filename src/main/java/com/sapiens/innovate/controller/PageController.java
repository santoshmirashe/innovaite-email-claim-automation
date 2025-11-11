package com.sapiens.innovate.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {
    @GetMapping("/homepage")
    public String homePage() {
        return "homepage";
    }

    @GetMapping({"/","/login-page"})
    public String login() {
        return "login";
    }

    @GetMapping("/register-page")
    public String registerPage() {
        return "register";
    }
}
