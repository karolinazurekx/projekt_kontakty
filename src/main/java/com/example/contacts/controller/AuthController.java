package com.example.contacts.controller;

import com.example.contacts.dto.LoginRequest;
import com.example.contacts.dto.LoginResponse;
import com.example.contacts.dto.RegisterRequest;
import com.example.contacts.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        try {
            authService.register(request);
            return ResponseEntity.ok("User registered");
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            LoginResponse resp = authService.login(request);
            return ResponseEntity.ok(resp);
        } catch (IllegalArgumentException | org.springframework.security.authentication.BadCredentialsException ex) {
            return ResponseEntity.status(401).body("Invalid credentials");
        }
    }
}