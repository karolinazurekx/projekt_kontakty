package com.example.contacts.security;

import org.springframework.security.core.userdetails.UserDetails;

/**
 * Interfejs JwtService
 * - D (Dependency Inversion): konsumenci zależą od abstrakcji
 * - I (Interface Segregation): tylko metody potrzebne do pracy z tokenem
 */
public interface JwtService {
    String generateToken(UserDetails userDetails);
    String extractUsername(String token);
    <T> T extractClaim(String token, java.util.function.Function<io.jsonwebtoken.Claims, T> resolver);
    boolean validateToken(String token);
}