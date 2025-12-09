package com.example.contacts.service;

import com.example.contacts.dto.LoginRequest;
import com.example.contacts.dto.LoginResponse;
import com.example.contacts.dto.RegisterRequest;

public interface AuthService {
    void register(RegisterRequest request);
    LoginResponse login(LoginRequest request);
}