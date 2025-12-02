package com.example.contacts.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "contacts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Contact {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Imię
    private String firstName;

    // Nazwisko
    private String lastName;

    // Email
    private String email;

    // Telefon
    private String phone;

    // Właściciel kontaktu – login użytkownika
    @Column(nullable = false)
    private String ownerUsername;
}
