package com.p03.Practica3.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class HomeController {

    @GetMapping("/home")
    public String home() {
        return "home"; // este busca `home.html` en `src/main/resources/templates`
    }
}
