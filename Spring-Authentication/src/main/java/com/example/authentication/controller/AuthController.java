package com.example.authentication.controller;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Demonstrates the authentication steps EXPLICITLY and manually, so you
 * can see exactly what the filters normally do behind the scenes.
 *
 * Try:
 *   POST /api/auth/login   { "username": "john", "password": "password123" }
 */
@RestController
public class AuthController {

    private final AuthenticationManager authenticationManager;

    public AuthController(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    public record LoginRequest(String username, String password) {}

    @PostMapping("/api/auth/login")
    public Map<String, Object> login(@RequestBody LoginRequest request) {

        // STEP 3: build an UNauthenticated Authentication token from raw credentials
        UsernamePasswordAuthenticationToken authRequest =
                new UsernamePasswordAuthenticationToken(request.username(), request.password());

        try {
            // STEP 4-8: AuthenticationManager -> AuthenticationProvider ->
            // UserDetailsService -> PasswordEncoder.matches() -> populated Authentication
            Authentication authResult = authenticationManager.authenticate(authRequest);

            // STEP 9: store the authenticated principal in the SecurityContext
            // (only meaningful for the rest of THIS request/thread since we're stateless;
            // in a real app you'd issue a JWT here instead and return it to the client)
            SecurityContextHolder.getContext().setAuthentication(authResult);

            return Map.of(
                    "status", "AUTHENTICATED",
                    "username", authResult.getName(),
                    "authorities", authResult.getAuthorities().toString(),
                    "note", "In production, issue a JWT here instead of relying on server state."
            );
        } catch (BadCredentialsException ex) {
            return Map.of(
                    "status", "REJECTED",
                    "reason", "Invalid username or password"
            );
        }
    }
}
