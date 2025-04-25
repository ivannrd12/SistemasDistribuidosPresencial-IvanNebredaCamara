package com.sprintboot.practica2.controller;

import com.sprintboot.practica2.model.usuario;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import com.sprintboot.practica2.repository.usuarioRepository;

import java.util.List;

@Controller
public class vistaUsuarioController {

    private final usuarioRepository usuarioRepository;

    public vistaUsuarioController(usuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping("/usuarios/lista")
    public String mostrarUsuarios(Model model) {
        List<usuario> usuarios = usuarioRepository.findAll();
        model.addAttribute("usuarios", usuarios);
        return "lista_usuarios";
    }

}
