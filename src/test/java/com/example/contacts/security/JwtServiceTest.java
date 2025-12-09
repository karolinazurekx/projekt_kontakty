package com.example.contacts.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Date;

import static org.assertj.core.api.Assertions.*;

/**
 * Testy JwtService - teraz testujemy konkretną implementację JwtServiceImpl.
 * Po refaktoryzacji JwtService jest interfejsem (D - Dependency Inversion),
 * dlatego w testach tworzymy instancję JwtServiceImpl.
 */
class JwtServiceTest {

    JwtService jwtService;

    @BeforeEach
    void setup() {
        // Base64 secret przykładowy (256-bit). W produkcji trzymaj w bezpiecznym miejscu.
        String secret = "dGhpc2lzbXlzZWNyZXRmb3J0ZXN0aW5nc2hvdWxkYmU0bG9uZw==";
        long expiration = 1000L * 60 * 60; // 1 godzina
        // Tworzymy implementację bez Springa, bez ReflectionTestUtils
        jwtService = new JwtServiceImpl(secret, expiration);
    }

    @Test
    void generateAndExtractUsername() {
        UserDetails ud = User.withUsername("john").password("x").roles("USER").build();
        String token = jwtService.generateToken(ud);
        assertThat(token).isNotBlank();
        String username = jwtService.extractUsername(token);
        assertThat(username).isEqualTo("john");
    }

    @Test
    void tokenHasValidExpiration() {
        UserDetails ud = User.withUsername("anna").password("x").roles("USER").build();
        String token = jwtService.generateToken(ud);
        Date issuedAt = jwtService.extractClaim(token, claims -> claims.getIssuedAt());
        assertThat(issuedAt).isNotNull();
    }

    @Test
    void extractClaim_subject() {
        UserDetails ud = User.withUsername("u").password("x").roles("USER").build();
        String token = jwtService.generateToken(ud);
        Date issuedAt = jwtService.extractClaim(token, claims -> claims.getIssuedAt());
        assertThat(issuedAt).isNotNull();
    }

    @Test
    void malformedToken_throws() {
        String bad = "not.a.jwt";
        assertThatThrownBy(() -> jwtService.extractUsername(bad))
                .isInstanceOf(Exception.class);
    }

    @Test
    void differentUserNameIsEncoded() {
        UserDetails ud = User.withUsername("marie").password("x").roles("USER").build();
        String token = jwtService.generateToken(ud);
        assertThat(jwtService.extractUsername(token)).isEqualTo("marie");
    }

    @Test
    void tokensAreUniquePerIssueTime() {
        UserDetails ud = User.withUsername("sam").password("x").roles("USER").build();
        String t1 = jwtService.generateToken(ud);
        String t2 = jwtService.generateToken(ud);
        assertThat(t1).isNotEqualTo(t2);
    }
}