package com.example.contacts.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
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
    @NotBlank(message = "Imię jest wymagane")
    @Size(max = 100)
    private String firstName;

    // Nazwisko
    @NotBlank(message = "Nazwisko jest wymagane")
    @Size(max = 100)
    private String lastName;

    // Email
    @NotBlank(message = "Email jest wymagany")
    @Email(message = "Nieprawidłowy format email")
    @Size(max = 200)
    private String email;

    // Telefon — dokładnie 9 cyfr
    @NotBlank(message = "Telefon jest wymagany")
    @Pattern(regexp = "^[0-9]{9}$", message = "Telefon musi zawierać dokładnie 9 cyfr")
    private String phone;

    // Właściciel kontaktu – login użytkownika
    @Column(nullable = false)
    private String ownerUsername;
}