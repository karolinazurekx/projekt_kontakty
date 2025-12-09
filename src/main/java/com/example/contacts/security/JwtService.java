package com.example.contacts.security;

public interface JwtService {
    String generateToken(org.springframework.security.core.userdetails.UserDetails userDetails);
    String extractUsername(String token);
    <T> T extractClaim(String token, java.util.function.Function<io.jsonwebtoken.Claims, T> resolver);
}