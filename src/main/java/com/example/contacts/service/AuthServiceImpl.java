package com.example.contacts.service;

import com.example.contacts.dto.LoginRequest;
import com.example.contacts.dto.LoginResponse;
import com.example.contacts.dto.RegisterRequest;
import com.example.contacts.model.AppUser;
import com.example.contacts.repository.UserRepository;
import com.example.contacts.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthServiceImpl(AuthenticationManager authManager,
                           UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           JwtService jwtService) {
        this.authManager = authManager;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Override
    public void register(RegisterRequest request) {
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Username already exists");
        }
        AppUser user = AppUser.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .role("ROLE_USER")
                .build();
        userRepository.save(user);
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        var opt = userRepository.findByUsername(request.getUsername());
        if (opt.isEmpty()) throw new IllegalArgumentException("Invalid credentials");

        // authenticate (may throw BadCredentialsException)
        authManager.authenticate(new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

        var user = opt.get();
        var jwt = jwtService.generateToken(new User(
                user.getUsername(),
                user.getPassword(),
                List.of(() -> user.getRole())
        ));
        return new LoginResponse(jwt);
    }
}