package com.example.contacts.integration;

import com.example.contacts.ContactsApplication;
import com.example.contacts.model.AppUser;
import com.example.contacts.model.Contact;
import com.example.contacts.repository.ContactRepository;
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

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// These tests assume JwtService is wired and /auth/login returns a Bearer token that can be used.
// They perform basic create/read/update/delete flows for a user.
@SpringBootTest(classes = ContactsApplication.class)
@AutoConfigureMockMvc
@AutoConfigureTestDatabase
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ContactsCrudIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ContactRepository contactRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    ObjectMapper objectMapper = new ObjectMapper();

    String token;

    @BeforeAll
    void setup() throws Exception {
        userRepository.deleteAll();
        contactRepository.deleteAll();
        userRepository.save(AppUser.builder().username("cruduser").password(passwordEncoder.encode("pass")).role("ROLE_USER").build());

        var body = objectMapper.writeValueAsString(Map.of("username","cruduser","password","pass"));
        String resp = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        token = objectMapper.readTree(resp).get("token").asText();
    }

    // 1. create contact
    @Test
    void createContact() throws Exception {
        Contact c = Contact.builder().firstName("C").lastName("D").email("c@d").phone("123456789").build();
        String body = objectMapper.writeValueAsString(c);
        mockMvc.perform(post("/api/contacts")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists());
    }

    // 2. read contacts
    @Test
    void readContacts() throws Exception {
        mockMvc.perform(get("/api/contacts")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    // 3. update contact
    @Test
    void updateContact() throws Exception {
        Contact saved = contactRepository.save(Contact.builder().firstName("U").lastName("V").email("u@v").phone("123456789").ownerUsername("cruduser").build());
        saved.setFirstName("Updated");
        String body = objectMapper.writeValueAsString(saved);
        mockMvc.perform(put("/api/contacts/" + saved.getId())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Updated"));
    }

    // 4. delete contact
    @Test
    void deleteContact() throws Exception {
        Contact saved = contactRepository.save(Contact.builder().firstName("T").lastName("Del").email("t@d").phone("123456789").ownerUsername("cruduser").build());
        mockMvc.perform(delete("/api/contacts/" + saved.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());
    }

    // 5. export json endpoint
    @Test
    void exportJson() throws Exception {
        mockMvc.perform(get("/api/contacts/export/json")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }
}