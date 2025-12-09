package com.example.contacts.controller;

import com.example.contacts.dto.LoginRequest;
import com.example.contacts.dto.LoginResponse;
import com.example.contacts.dto.RegisterRequest;
import com.example.contacts.model.AppUser;
import com.example.contacts.repository.UserRepository;
import com.example.contacts.security.JwtService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

/**
 * AuthController
 * - S: odpowiada jedynie za REST endpoints związane z auth
 * - D: zależy od abstrakcji JwtService (interfejs)
 * - O: można dodać dodatkowe endpointy (np. refresh token) bez modyfikacji istniejących
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationManager authManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthController(AuthenticationManager authManager,
                          UserRepository userRepository,
                          PasswordEncoder passwordEncoder,
                          JwtService jwtService) {
        this.authManager = authManager;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        Optional<AppUser> maybe = userRepository.findByUsername(request.getUsername());
        if (maybe.isPresent()) {
            return ResponseEntity.badRequest().body("Username already exists");
        }

        AppUser user = AppUser.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .role("ROLE_USER")
                .build();

        userRepository.save(user);
        return ResponseEntity.ok("User registered");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(401).body("Invalid credentials");
        }

        var user = userRepository.findByUsername(request.getUsername()).orElse(null);
        if (user == null) {
            return ResponseEntity.status(401).body("Invalid credentials");
        }

        var jwt = jwtService.generateToken(User.withUsername(user.getUsername())
                .password(user.getPassword())
                .authorities(() -> user.getRole())
                .build());

        return ResponseEntity.ok(new LoginResponse(jwt));
    }

    @GetMapping("/me")
    public ResponseEntity<?> me() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        return ResponseEntity.ok(java.util.Map.of(
                "username", auth.getName(),
                "role", auth.getAuthorities().iterator().next().getAuthority()
        ));
    }
}