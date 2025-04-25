package com.sprintboot.practica2.repository;

import com.sprintboot.practica2.model.usuario;
import org.springframework.data.jpa.repository.JpaRepository;

public interface usuarioRepository extends JpaRepository<usuario, Long> {
}
