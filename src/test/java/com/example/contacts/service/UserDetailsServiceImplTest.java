package com.example.contacts.service;

import com.example.contacts.model.AppUser;
import com.example.contacts.repository.UserRepository;
import org.junit.jupiter.api.*;
import org.mockito.*;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserDetailsServiceImplTest {

    @Mock
    UserRepository userRepository;

    UserDetailsServiceImpl service;

    AutoCloseable mocks;

    @BeforeEach
    void setup() {
        mocks = MockitoAnnotations.openMocks(this);
        service = new UserDetailsServiceImpl(userRepository);
    }

    @AfterEach
    void tearDown() throws Exception {
        mocks.close();
    }

    // 1. load existing user
    @Test
    void loadUserByUsername_exists() {
        AppUser u = AppUser.builder().username("bob").password("enc").role("ROLE_USER").build();
        when(userRepository.findByUsername("bob")).thenReturn(Optional.of(u));
        UserDetails ud = service.loadUserByUsername("bob");
        assertThat(ud.getUsername()).isEqualTo("bob");
    }

    // 2. missing user throws
    @Test
    void loadUserByUsername_missing() {
        when(userRepository.findByUsername("no")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.loadUserByUsername("no")).isInstanceOf(UsernameNotFoundException.class);
    }

    // 3. role conversion ROLE_USER -> USER
    @Test
    void roleConversion() {
        AppUser u = AppUser.builder().username("r").password("p").role("ROLE_ADMIN").build();
        when(userRepository.findByUsername("r")).thenReturn(Optional.of(u));
        UserDetails ud = service.loadUserByUsername("r");
        assertThat(ud.getAuthorities()).anyMatch(a -> a.getAuthority().contains("ADMIN"));
    }

    // 4. password is present in returned UserDetails
    @Test
    void passwordIsSet() {
        AppUser u = AppUser.builder().username("p").password("pwd").role("ROLE_USER").build();
        when(userRepository.findByUsername("p")).thenReturn(Optional.of(u));
        UserDetails ud = service.loadUserByUsername("p");
        assertThat(ud.getPassword()).isEqualTo("pwd");
    }
}