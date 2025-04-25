package com.sprintboot.practica2.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class loginController {

    @GetMapping("/login")
    public String mostrarFormularioLogin() {

        return "login"; // Esto carga templates/login.html
    }
}
