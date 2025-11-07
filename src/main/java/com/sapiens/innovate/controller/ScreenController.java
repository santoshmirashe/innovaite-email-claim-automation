package com.sapiens.innovate.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ScreenController {
    @GetMapping("/homepage")
    public String homePage() {
        return "homepage";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }
    @GetMapping("/")
    public String loginn() {
        return "login";
    }
}
