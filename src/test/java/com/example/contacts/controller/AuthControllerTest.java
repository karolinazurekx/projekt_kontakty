package com.example.contacts.controller;

import com.example.contacts.TestSecurityUtils;
import com.example.contacts.dto.LoginRequest;
import com.example.contacts.dto.LoginResponse;
import com.example.contacts.dto.RegisterRequest;
import com.example.contacts.model.AppUser;
import com.example.contacts.repository.UserRepository;
import com.example.contacts.security.JwtService;
import org.junit.jupiter.api.*;
import org.mockito.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthControllerTest {

    @Mock
    AuthenticationManager authManager;

    @Mock
    UserRepository userRepository;

    @Mock
    PasswordEncoder passwordEncoder;

    @Mock
    JwtService jwtService;

    @InjectMocks
    AuthController authController;

    AutoCloseable mocks;

    @BeforeEach
    void setup() {
        mocks = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        mocks.close();
    }

    // 1. register success
    @Test
    void register_success() {
        RegisterRequest req = new RegisterRequest();
        req.setUsername("u1");
        req.setPassword("p1");

        when(userRepository.findByUsername("u1")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("p1")).thenReturn("enc");

        ResponseEntity<?> res = authController.register(req);
        assertThat(res.getStatusCodeValue()).isEqualTo(200);
        verify(userRepository).save(ArgumentMatchers.any(AppUser.class));
    }

    // 2. register username exists
    @Test
    void register_usernameExists() {
        RegisterRequest req = new RegisterRequest();
        req.setUsername("u2");
        req.setPassword("p2");

        when(userRepository.findByUsername("u2")).thenReturn(Optional.of(AppUser.builder().username("u2").build()));
        ResponseEntity<?> res = authController.register(req);
        assertThat(res.getStatusCodeValue()).isEqualTo(400);
    }

    // 3. login success
    @Test
    void login_success() {
        LoginRequest req = new LoginRequest();
        req.setUsername("john");
        req.setPassword("pass");

        when(authManager.authenticate(any())).thenReturn(null);
        AppUser user = AppUser.builder().username("john").password("enc").role("ROLE_USER").build();
        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));
        when(jwtService.generateToken(any())).thenReturn("tok");

        ResponseEntity<?> res = authController.login(req);
        assertThat(res.getStatusCodeValue()).isEqualTo(200);
        assertThat(((LoginResponse) res.getBody()).getToken()).isEqualTo("tok");
    }

    // 4. login bad credentials
    @Test
    void login_badCredentials() {
        LoginRequest req = new LoginRequest();
        req.setUsername("no");
        req.setPassword("pw");

        doThrow(new BadCredentialsException("bad")).when(authManager).authenticate(any());
        ResponseEntity<?> res = authController.login(req);
        assertThat(res.getStatusCodeValue()).isEqualTo(401);
    }

    // 5. me returns authenticated name and role
    @Test
    void me_returnsInfo() {
        // set SecurityContext
        TestSecurityUtils.setAuthentication("me", "ROLE_USER");
        ResponseEntity<?> res = authController.me();
        assertThat(res.getStatusCodeValue()).isEqualTo(200);
        @SuppressWarnings("unchecked")
        var body = (java.util.Map<String, String>) res.getBody();
        assertThat(body.get("username")).isEqualTo("me");
    }

    // 6. register persists role ROLE_USER by default
    @Test
    void register_savesRoleUser() {
        RegisterRequest req = new RegisterRequest();
        req.setUsername("newu");
        req.setPassword("p");

        when(userRepository.findByUsername("newu")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("p")).thenReturn("enc");

        authController.register(req);
        ArgumentCaptor<AppUser> cap = ArgumentCaptor.forClass(AppUser.class);
        verify(userRepository).save(cap.capture());
        assertThat(cap.getValue().getRole()).isEqualTo("ROLE_USER");
    }
}