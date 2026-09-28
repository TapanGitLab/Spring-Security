package com.example.authentication.repository;

import com.example.authentication.model.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<AppUser, Long> {

    // STEP 6: UserDetailsService calls something like this to load a user by username
    Optional<AppUser> findByUsername(String username);
}
