package com.example.contacts.dto;

import lombok.Data;

/**
 * - S: tylko dane logowania
 */
@Data
public class LoginRequest {
    private String username;
    private String password;
}