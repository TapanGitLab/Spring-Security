package com.example.authentication.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Protected endpoints. These are reached only AFTER the filter chain has
 * authenticated the request (via HTTP Basic here) and the URL-based
 * authorization rules in SecurityConfig have passed.
 *
 * Try with curl, supplying HTTP Basic credentials:
 *   curl -u john:password123  http://localhost:8080/api/user/hello
 *   curl -u admin:admin123    http://localhost:8080/api/admin/hello
 *   curl -u john:password123  http://localhost:8080/api/admin/hello   -> 403 Forbidden
 */
@RestController
public class HelloController {

    // Any USER or ADMIN can access (see SecurityConfig authorizeHttpRequests rule)
    @GetMapping("/api/user/hello")
    public Map<String, String> userHello(Authentication authentication) {
        return Map.of(
                "message", "Hello, " + authentication.getName() + "! You are authenticated.",
                "authorities", authentication.getAuthorities().toString()
        );
    }

    // Only ADMIN can access
    @GetMapping("/api/admin/hello")
    public Map<String, String> adminHello(Authentication authentication) {
        return Map.of(
                "message", "Welcome, admin " + authentication.getName() + "!",
                "authorities", authentication.getAuthorities().toString()
        );
    }
}
