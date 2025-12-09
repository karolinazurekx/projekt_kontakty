package com.example.contacts.dto;

import lombok.Data;

/**
 * DTO: RegisterRequest
 * - S: tylko dane rejestracyjne
 */
@Data
public class RegisterRequest {
    private String username;
    private String password;
}