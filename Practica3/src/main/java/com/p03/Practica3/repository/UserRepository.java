package com.p03.Practica3.repository;

import com.p03.Practica3.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository {

    Optional<User> findByUsername(String username);
}
