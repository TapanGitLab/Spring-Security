# Spring-Authentication

Part of the **Spring-Security** project collection — a set of small, runnable
Spring Boot projects, each demonstrating one Spring Security concept in
depth. This project covers **Authentication**: how Spring Security verifies
"who is this user?", end to end.

Uses an H2 in-memory database so it runs with **zero external setup**.

## Run it

```bash
mvn spring-boot:run
```

App starts on `http://localhost:8080`. Two users are seeded automatically:

| username | password    | role  |
|----------|-------------|-------|
| john     | password123 | USER  |
| admin    | admin123    | ADMIN |

## Try the flow

### 1. Manual authentication walkthrough (see each step explicitly)
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"john","password":"password123"}'
```
Watch `AuthController.login()` — it calls `AuthenticationManager.authenticate()`
directly so you can see each step of the flow happen in your own code.

Try a wrong password too, to see the `BadCredentialsException` path:
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"john","password":"wrong"}'
```

### 2. Normal filter-chain authentication (HTTP Basic) + authorization
```bash
curl -u john:password123  http://localhost:8080/api/user/hello
curl -u admin:admin123    http://localhost:8080/api/admin/hello
curl -u john:password123  http://localhost:8080/api/admin/hello   # -> 403 Forbidden
curl http://localhost:8080/api/user/hello                          # -> 401 Unauthorized
```

### 3. Browse the H2 database
`http://localhost:8080/h2-console`
JDBC URL: `jdbc:h2:mem:authenticationdemo`, user: `sa`, password: blank.

## File map → authentication steps

| Step | File |
|------|------|
| 1–2: Filter intercepts request, extracts credentials | Handled automatically by `BasicAuthenticationFilter` (built into Spring Security) |
| 3: Build unauthenticated `Authentication` token | `AuthController.login()` |
| 4: `AuthenticationManager` delegates to a provider | `SecurityConfig.authenticationManager()` / `authenticationProvider()` |
| 5: `UserDetailsService` loads the user | `CustomUserDetailsService.java` |
| 6: User record fetched from DB | `UserRepository.java`, `AppUser.java` |
| 7: `PasswordEncoder` compares hashes | `SecurityConfig.passwordEncoder()` (BCrypt) |
| 8–9: Populated `Authentication` stored in context | `AuthController.login()` (manual) / done automatically by filters for `/api/user/**`, `/api/admin/**` |
| 10: Authorization rules checked | `SecurityConfig.securityFilterChain()` `.authorizeHttpRequests(...)` |

## Package structure

```
com.example.authentication
├── AuthenticationApplication.java   (main class + demo user seeding)
├── config/SecurityConfig.java       (filter chain, PasswordEncoder, AuthenticationManager)
├── controller/AuthController.java   (manual login walkthrough)
├── controller/HelloController.java  (protected endpoints)
├── model/AppUser.java               (JPA entity)
├── repository/UserRepository.java   (Spring Data JPA)
└── service/CustomUserDetailsService.java
```

## About the Spring-Security collection

This repo lives under a **`Spring-Security`** GitHub organization/folder
alongside sibling projects, one per concept, e.g.:

```
Spring-Security/
├── Spring-Authentication/
├── Spring-Authentication-Authorization/
├── Spring-JWT/
├── Spring-OAuth2-Login/
├── Spring-OAuth2-Resource-Server/
├── Spring-CSRF-CORS/
├── Spring-Method-Security/
└── Spring-Session-Management/
```

Suggested convention for naming as you add more:

| Concept | Repo name |
|---|---|
| Authentication (this repo) | `Spring-Authentication` |
| Authorization (RBAC, URL & method-level rules) | `Spring-Authentication-Authorization` |
| JWT-based stateless auth | `Spring-JWT` |
| OAuth2 / OIDC social login | `Spring-OAuth2-Login` |
| OAuth2 Resource Server (validating tokens from an external Auth Server) | `Spring-OAuth2-Resource-Server` |
| CSRF & CORS | `Spring-CSRF-CORS` |
| Method-Level Security (`@PreAuthorize`, `@Secured`) | `Spring-Method-Security` |
| Session Management (fixation, concurrent sessions) | `Spring-Session-Management` |

Each repo is self-contained and runnable independently — no shared parent
POM required — so anyone can clone just one folder and run it on its own.

## Notes / next steps

- This demo uses **HTTP Basic** + **stateless sessions** for simplicity —
  credentials are sent on every request. `Spring-JWT` will show issuing/
  validating a token instead.
- Passwords are always stored as **BCrypt hashes** — never plain text.
- Swap the seeded users for a real user-registration flow when adapting this.
