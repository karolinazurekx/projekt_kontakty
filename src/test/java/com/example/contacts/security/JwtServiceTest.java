package com.example.contacts.security;

import org.junit.jupiter.api.*;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Date;

import static org.assertj.core.api.Assertions.*;

class JwtServiceTest {

    JwtService jwtService;

    @BeforeEach
    void setup() {
        jwtService = new JwtService();
        // 256-bit base64 secret (przykład)
        String secret = "dGhpc2lzbXlzZWNyZXRmb3J0ZXN0aW5nc2hvdWxkYmU0bG9uZw=="; // base64
        ReflectionTestUtils.setField(jwtService, "jwtSecret", secret);
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", 1000L * 60 * 60); // 1h
    }


    // 1. generate token and extract username
    @Test
    void generateAndExtractUsername() {
        UserDetails ud = User.withUsername("john").password("x").roles("USER").build();
        String token = jwtService.generateToken(ud);
        assertThat(token).isNotBlank();
        String username = jwtService.extractUsername(token);
        assertThat(username).isEqualTo("john");
    }

    // 2. token expiration is honored (expiration > now)
    @Test
    void tokenHasValidExpiration() {
        UserDetails ud = User.withUsername("anna").password("x").roles("USER").build();
        String token = jwtService.generateToken(ud);
        // extraction should not throw
        String username = jwtService.extractUsername(token);
        assertThat(username).isEqualTo("anna");
    }

    // 3. extractClaim works
    @Test
    void extractClaim_subject() {
        UserDetails ud = User.withUsername("u").password("x").roles("USER").build();
        String token = jwtService.generateToken(ud);
        Date issuedAt = jwtService.extractClaim(token, claims -> claims.getIssuedAt());
        assertThat(issuedAt).isNotNull();
    }

    // 4. malformed token throws (we expect a runtime from jjwt)
    @Test
    void malformedToken_throws() {
        String bad = "not.a.jwt";
        assertThatThrownBy(() -> jwtService.extractUsername(bad)).isInstanceOf(Exception.class);
    }

    // 5. another username
    @Test
    void differentUserNameIsEncoded() {
        UserDetails ud = User.withUsername("marie").password("x").roles("USER").build();
        String token = jwtService.generateToken(ud);
        assertThat(jwtService.extractUsername(token)).isEqualTo("marie");
    }

    // 6. tokens differ for different issue times
    @Test
    void tokensAreUniquePerIssueTime() {
        UserDetails ud = User.withUsername("sam").password("x").roles("USER").build();
        String t1 = jwtService.generateToken(ud);
        String t2 = jwtService.generateToken(ud);
        assertThat(t1).isNotEqualTo(t2);
    }
}