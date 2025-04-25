package com.sprintboot.practica2.controller;

import com.sprintboot.practica2.model.usuario;
import org.springframework.web.bind.annotation.*;
import com.sprintboot.practica2.repository.usuarioRepository;

import java.util.List;

@RestController
@RequestMapping("/usuarios")
public class usuarioController {

    private final usuarioRepository usuarioRepository;

    public usuarioController(usuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping
    public List<usuario> listaUsuarios() {

        return usuarioRepository.findAll();
    }

    @PostMapping
    public usuario crearUsuario(@RequestBody usuario usuario) {
        return usuarioRepository.save(usuario);
    }
}
