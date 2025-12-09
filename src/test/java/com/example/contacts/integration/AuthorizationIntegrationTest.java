package com.example.contacts.integration;

import com.example.contacts.ContactsApplication;
import com.example.contacts.model.AppUser;
import com.example.contacts.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// testy post
@SpringBootTest(classes = ContactsApplication.class)
@AutoConfigureMockMvc
@AutoConfigureTestDatabase
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AuthorizationIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    UserRepository userRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    ObjectMapper objectMapper = new ObjectMapper();

    @BeforeAll
    void initUsers() {
        userRepository.deleteAll();
        userRepository.save(AppUser.builder().username("intuser").password(passwordEncoder.encode("pass")).role("ROLE_USER").build());
        userRepository.save(AppUser.builder().username("intadmin").password(passwordEncoder.encode("adminp")).role("ROLE_ADMIN").build());
    }

    // 1. login returns token
    @Test
    void loginReturnsToken() throws Exception {
        var body = objectMapper.writeValueAsString(java.util.Map.of("username","intuser","password","pass"));
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists());
    }

    // 2. unauthenticated access to protected endpoint fails
    @Test
    void protectedEndpointRequiresAuth() throws Exception {
        // Niektóre środowiska mogą zwracać 401 lub 403 — akceptujemy dowolny kod 4xx.
        mockMvc.perform(get("/api/contacts"))
                .andExpect(status().is4xxClientError());
    }

    // 3. registration creates user
    @Test
    void registerCreatesUser() throws Exception {
        var body = objectMapper.writeValueAsString(java.util.Map.of("username","ruser","password","rp"));
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());
        Assertions.assertTrue(userRepository.findByUsername("ruser").isPresent());
    }
}