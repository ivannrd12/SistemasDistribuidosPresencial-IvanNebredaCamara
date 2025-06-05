package com.p03.Practica3.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.p03.Practica3.model.Pista;

public interface PistaRepository extends JpaRepository<Pista, Long> {
}
