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
            // -> estamos editando un usuario existente
            User existente = userRepository.findById(usuario.getId())
                    .orElseThrow(() -> new IllegalArgumentException("ID de usuario inválido"));

            if (usuario.getPassword() == null || usuario.getPassword().isBlank()) {
                // El admin no escribió nueva contraseña: mantener el hash actual
                usuario.setPassword(existente.getPassword());
            } else {
                // El admin sí escribió algo en el campo password: cifrarlo con BCrypt
                String nuevoHash = new BCryptPasswordEncoder().encode(usuario.getPassword());
                usuario.setPassword(nuevoHash);
            }
        } else {
            // -> es un nuevo usuario: la contraseña siempre se cifra
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

    @GetMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Long id) {
        userRepository.deleteById(id);
        return "redirect:/admin/usuarios";
    }

}
