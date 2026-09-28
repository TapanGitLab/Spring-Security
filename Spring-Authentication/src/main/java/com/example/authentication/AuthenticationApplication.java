package com.example.authentication;

import com.example.authentication.model.AppUser;
import com.example.authentication.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Entry point. Also seeds two demo users on startup so the app is
 * runnable immediately without manual DB setup.
 *
 * Demo credentials:
 *   username: john   password: password123   role: USER
 *   username: admin  password: admin123      role: ADMIN
 */
@SpringBootApplication
public class AuthenticationApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthenticationApplication.class, args);
    }

    /**
     * STEP 6 (from the auth flow): seed users with an ALREADY-HASHED
     * password. We never store plain text passwords — PasswordEncoder
     * hashes them once here, and again at comparison time during login.
     */
    @Bean
    CommandLineRunner seedUsers(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (userRepository.findByUsername("john").isEmpty()) {
                AppUser john = new AppUser();
                john.setUsername("john");
                john.setPassword(passwordEncoder.encode("password123"));
                john.setRole("USER");
                userRepository.save(john);
            }
            if (userRepository.findByUsername("admin").isEmpty()) {
                AppUser admin = new AppUser();
                admin.setUsername("admin");
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.setRole("ADMIN");
                userRepository.save(admin);
            }
        };
    }
}
