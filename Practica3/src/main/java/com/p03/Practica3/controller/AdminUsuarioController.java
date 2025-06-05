package com.p03.Practica3.controller;

import com.p03.Practica3.model.User;
import com.p03.Practica3.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Controller
@RequestMapping("/admin/usuarios")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUsuarioController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public String listarUsuarios(Model model) {
        model.addAttribute("usuarios", userRepository.findAll());
        return "admin/usuarios";
    }

    @GetMapping("/nuevo")
    public String mostrarFormularioNuevo(Model model) {
        model.addAttribute("usuario", new User());
        return "admin/formUsuario";
    }

    @PostMapping("/guardar")
    public String guardar(@ModelAttribute("usuario") User usuario) {
        if (usuario.getId() != null) {

            User existente = userRepository.findById(usuario.getId())
                    .orElseThrow(() -> new IllegalArgumentException("ID de usuario inválido"));

            if (usuario.getPassword() == null || usuario.getPassword().isBlank()) {

                usuario.setPassword(existente.getPassword());
            } else {

                String nuevoHash = new BCryptPasswordEncoder().encode(usuario.getPassword());
                usuario.setPassword(nuevoHash);
            }
        } else {

            String hash = new BCryptPasswordEncoder().encode(usuario.getPassword());
            usuario.setPassword(hash);
        }

        usuario.setEnabled(true);
        userRepository.save(usuario);
        return "redirect:/admin/usuarios";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model) {
        Optional<User> usuario = userRepository.findById(id);
        usuario.ifPresent(u -> model.addAttribute("usuario", u));
        return "admin/formUsuario";
    }

    @PostMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Long id) {
        userRepository.deleteById(id);
        return "redirect:/admin/usuarios";
    }

}
