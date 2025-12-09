package com.example.contacts;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.example.contacts.repository.UserRepository;
import com.example.contacts.repository.ContactRepository;
import com.example.contacts.model.AppUser;
import com.example.contacts.model.Contact;

@Configuration
public class ContactsApliccationConfig {

    @Bean
    CommandLineRunner seedData(UserRepository userRepository,
                               ContactRepository contactRepository,
                               PasswordEncoder passwordEncoder) {
        return args -> {
            if (userRepository.findByUsername("cruduser").isEmpty()) {
                AppUser u = AppUser.builder()
                        .username("cruduser")
                        .password(passwordEncoder.encode("pass"))
                        .role("ROLE_USER")
                        .build();
                userRepository.save(u);
                System.out.println(">>> CREATED TEST USER: cruduser / pass");
            }
            if (contactRepository.count() == 0) {
                Contact c = Contact.builder()
                        .firstName("Jan")
                        .lastName("Kowalski")
                        .email("jan.k@example.com")
                        .phone("123456789")
                        .ownerUsername("cruduser")
                        .build();
                contactRepository.save(c);
                System.out.println(">>> CREATED TEST CONTACT");
            }
        };
    }
}