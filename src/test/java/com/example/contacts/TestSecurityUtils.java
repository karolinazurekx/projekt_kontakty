package com.example.contacts;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

public class TestSecurityUtils {

    public static void setAuthentication(String username, String role) {
        SecurityContext ctx = SecurityContextHolder.createEmptyContext();
        Authentication auth = new UsernamePasswordAuthenticationToken(
                username,
                null,
                List.of(new SimpleGrantedAuthority(role))
        );
        ctx.setAuthentication(auth);
        SecurityContextHolder.setContext(ctx);
    }

    public static void clear() {
        SecurityContextHolder.clearContext();
    }
}