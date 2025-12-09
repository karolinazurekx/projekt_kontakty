package com.example.contacts.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**

 * - S: opakowanie tokenu w odpowiedzi
 */
@Data
@AllArgsConstructor
public class LoginResponse {
    private String token;
}