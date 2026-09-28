package com.example.authentication.service;

import com.example.authentication.model.AppUser;
import com.example.authentication.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * STEP 6 of the authentication flow:
 * DaoAuthenticationProvider calls loadUserByUsername() to fetch the user
 * from our data source (H2 via JPA here) and converts it into Spring
 * Security's own UserDetails representation.
 *
 * This is the ONE place that bridges "your app's user model" (AppUser)
 * to "Spring Security's user model" (UserDetails).
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AppUser appUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("No user found with username: " + username));

        // GrantedAuthority list — Spring Security expects roles prefixed with "ROLE_"
        // when using hasRole("ADMIN"); hasAuthority("ROLE_ADMIN") would be the raw equivalent.
        return User.builder()
                .username(appUser.getUsername())
                .password(appUser.getPassword()) // already BCrypt-hashed
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_" + appUser.getRole())))
                .build();
    }
}
