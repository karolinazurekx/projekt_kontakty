package com.example.contacts.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.UUID;
import java.util.function.Function;

/**
 * Implementacja JwtService
 * - S: odpowiedzialność - operacje na tokenach
 * - O: można zaimplementować alternatywny JwtService (np. RSA) bez zmian w klientach
 */
@Service
public class JwtServiceImpl implements JwtService {

    private final SecretKey signingKey;
    private final long jwtExpiration;

    public JwtServiceImpl(@Value("${jwt.secret}") String jwtSecret,
                          @Value("${jwt.expiration}") long jwtExpiration) {
        byte[] bytes = Decoders.BASE64.decode(jwtSecret);
        this.signingKey = Keys.hmacShaKeyFor(bytes);
        this.jwtExpiration = jwtExpiration;
    }

    @Override
    public String generateToken(UserDetails userDetails) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + jwtExpiration);
        return Jwts.builder()
                .setId(UUID.randomUUID().toString())
                .setSubject(userDetails.getUsername())
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }

    @Override
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    @Override
    public <T> T extractClaim(String token, Function<Claims, T> resolver) {
        Claims claims = Jwts.parser()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return resolver.apply(claims);
    }

    @Override
    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(signingKey).build().parseClaimsJws(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }
}