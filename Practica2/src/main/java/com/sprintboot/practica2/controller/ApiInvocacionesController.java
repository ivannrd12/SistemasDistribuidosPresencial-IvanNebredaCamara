package com.sprintboot.practica2.controller;

import com.sprintboot.practica2.service.ApiService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ApiInvocacionesController {

    private final ApiService apiService;

    public ApiInvocacionesController(ApiService apiService) {
        this.apiService = apiService;
    }

    @GetMapping("/api-invocaciones")
    public String mostrarApiInvocaciones(Model model) {
        return "api_invocaciones";
    }

    @GetMapping("/invocar/archivo")
    public String invocarArchivo(Model model) {
        String respuesta = apiService.simularErrorArchivo();
        model.addAttribute("resultado", respuesta);
        return "api_invocaciones";
    }

    @GetMapping("/invocar/basedatos")
    public String invocarBaseDatos(Model model) {
        String respuesta = apiService.simularErrorBaseDatos();
        model.addAttribute("resultado", respuesta);
        return "api_invocaciones";
    }

    @GetMapping("/invocar/pokemon")
    public String invocarPokemon(Model model) {
        String respuesta = apiService.simularErrorPokemon();
        model.addAttribute("resultado", respuesta);
        return "api_invocaciones";
    }
}
