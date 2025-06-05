package com.p03.Practica3.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ErrorController {

    @GetMapping("/error/403")
    public String accesoDenegado() {
        // Muestra una página sencilla que diga "No tienes permiso para ver esto."
        return "error/403";
    }

    // (Opcional) Atrapa AccessDeniedException si se lanza desde Spring Security
    @ExceptionHandler(AccessDeniedException.class)
    public String handleAccessDenied(AccessDeniedException ex, HttpServletRequest request, Model model) {
        model.addAttribute("mensaje", "No tienes permiso para acceder a esta sección.");
        return "error/403";
    }
}

