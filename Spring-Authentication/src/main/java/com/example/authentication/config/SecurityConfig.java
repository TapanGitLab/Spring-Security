package com.example.authentication.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Central security config. This wires together every actor in the
 * step-by-step authentication flow:
 *
 *   Filter chain -> AuthenticationManager -> AuthenticationProvider
 *   -> UserDetailsService -> PasswordEncoder -> SecurityContext
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity // enables @PreAuthorize / @PostAuthorize on service methods
public class SecurityConfig {

    private final UserDetailsService userDetailsService;

    public SecurityConfig(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    /**
     * STEP: PasswordEncoder — used both when SEEDING users (hash on save)
     * and when AUTHENTICATING (hash comparison via matches()).
     * BCrypt is adaptive/salted — never use plain MD5/SHA for passwords.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * STEP: AuthenticationProvider — the piece that actually calls
     * UserDetailsService + PasswordEncoder and decides pass/fail.
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    /**
     * STEP: AuthenticationManager — the entry point filters call to
     * trigger the whole authentication process. We expose it as a bean
     * so our own /api/auth/login controller can call it manually too.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * STEP: SecurityFilterChain — defines which URLs need what level of
     * access, and which auth mechanism(s) are active.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Stateless REST API: no CSRF tokens needed (no browser session/cookie to forge)
            .csrf(csrf -> csrf.disable())

            // URL-based authorization rules, evaluated top to bottom
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**", "/h2-console/**").permitAll()
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/user/**").hasAnyRole("USER", "ADMIN")
                .anyRequest().authenticated()
            )

            // No server-side session; every request must (re)authenticate
            // (form login below issues Basic-Auth-style credentials per request
            // for simplicity — swap for JWT filter in a production app)
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // HTTP Basic as the transport for credentials (simple to test with curl/Postman)
            .httpBasic(basic -> {})

            // Needed only so the H2 console's own frames aren't blocked
            .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()));

        return http.build();
    }
}
