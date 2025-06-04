package com.p03.Practica3;

import com.p03.Practica3.model.User;
import com.p03.Practica3.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class Practica3Application {

	public static void main(String[] args) {
		SpringApplication.run(Practica3Application.class, args);
	}

	@Bean
	CommandLineRunner init(UserRepository userRepository, PasswordEncoder encoder) {
		return args -> {
			if (userRepository.findByUsername("admin").isEmpty()) {
				User user = User.builder()
						.username("admin")
						.password(encoder.encode("admin123"))
						.role("ROLE_ADMIN")
						.enabled(true)
						.build();

				userRepository.save(user);
				System.out.println("🟢 Usuario admin creado");
			} else {
				System.out.println("🔵 Usuario admin ya existe");
			}
		};
	}

}
