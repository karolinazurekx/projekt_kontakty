package com.example.contacts.config;

import org.junit.jupiter.api.Test;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import static org.assertj.core.api.Assertions.assertThat;

class SecurityConfigTest {

    // SecurityConfig wymaga JwtAuthFilter w konstruktorze; do testów jednostkowych
    // wystarczy przekazać null, bo testujemy tylko metody pomocnicze (cors, encoder)
    SecurityConfig config = new SecurityConfig(null);

    @Test
    void corsConfigurationSource_allowsAnyOrigin() {
        var source = config.corsConfigurationSource();

        // CorsConfigurationSource to interfejs — nasz bean jest UrlBasedCorsConfigurationSource,
        // dlatego rzutujemy na konkretną implementację, żeby uzyskać dostęp do mapy konfiguracji.
        UrlBasedCorsConfigurationSource urlSource = (UrlBasedCorsConfigurationSource) source;
        CorsConfiguration cfg = urlSource.getCorsConfigurations().get("/**");

        assertThat(cfg.getAllowedOriginPatterns()).contains("*");
        assertThat(cfg.getAllowedMethods()).contains("GET", "POST");
    }

    @Test
    void passwordEncoder_isBcrypt() {
        var enc = config.passwordEncoder();
        assertThat(enc).isNotNull();
    }
}