package com.example.contacts.model;

import jakarta.persistence.*;
import lombok.*;

/**
 * Encja AppUser
 * - S: model/dane użytkownika
 */
@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String username;

    private String password;

    private String role; // np. ROLE_USER, ROLE_ADMIN
}