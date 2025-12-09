package com.example.contacts.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * DTO: LoginResponse
 * - S: opakowanie tokenu w odpowiedzi
 */
@Data
@AllArgsConstructor
public class LoginResponse {
    private String token;
}