package com.p03.Practica3.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import com.p03.Practica3.model.Pista;
import com.p03.Practica3.repository.PistaRepository;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/pistas")
@PreAuthorize("hasRole('ADMIN')")
public class PistaController {

    @Autowired
    private PistaRepository pistaRepository;

    @GetMapping
    public String listarPistas(Model model) {
        model.addAttribute("pistas", pistaRepository.findAll());
        return "pistas/pistas";
    }

    @GetMapping("/nueva")
    public String nuevaPista(Model model) {
        model.addAttribute("pista", new Pista());
        return "pistas/formPista";
    }

    @PostMapping("/guardar")
    public String guardarPista(@ModelAttribute Pista pista) {
        pistaRepository.save(pista);
        return "redirect:/admin/pistas";
    }

    @GetMapping("/editar/{id}")
    public String editarPista(@PathVariable Long id, Model model) {
        Pista pista = pistaRepository.findById(id).orElseThrow();
        model.addAttribute("pista", pista);
        return "pistas/formPista";
    }

    @PostMapping("/eliminar/{id}")
    public String eliminarPista(@PathVariable Long id) {
        pistaRepository.deleteById(id);
        return "redirect:/admin/pistas";
    }
}
