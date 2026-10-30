package com.project.banksphere.service;

import com.project.banksphere.dto.request.LoginRequest;
import com.project.banksphere.dto.request.RegisterRequest;
import com.project.banksphere.dto.response.LoginResponse;
import com.project.banksphere.dto.response.RegisterResponse;
import com.project.banksphere.entity.User;
import com.project.banksphere.enums.Role;
import com.project.banksphere.exception.UserAlreadyExistsException;
import com.project.banksphere.repository.UserRepository;
import com.project.banksphere.security.JwtService;
import com.project.banksphere.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthServiceImpl authService;

    private RegisterRequest validRequest;
    private LoginRequest validLoginRequest;
    private User testUser;

    @BeforeEach
    void setUp() {
        validRequest = new RegisterRequest(
                "Jane Doe",
                "jane.doe@example.com",
                "9876543210",
                "SecureP@ss123"
        );

        validLoginRequest = new LoginRequest(
                "jane.doe@example.com",
                "SecureP@ss123"
        );

        testUser = User.builder()
                .id(1L)
                .name("Jane Doe")
                .email("jane.doe@example.com")
                .mobile("9876543210")
                .password("$2a$10$encodedHashString")
                .role(Role.ROLE_CUSTOMER)
                .build();
        testUser.setCreatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("Should successfully register a new user and encode their password")
    void register_Success() {
        // Given
        when(userRepository.existsByEmail("jane.doe@example.com")).thenReturn(false);
        when(passwordEncoder.encode("SecureP@ss123")).thenReturn("$2a$10$encodedHashString");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        RegisterResponse response = authService.register(validRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.userId()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("Jane Doe");
        assertThat(response.email()).isEqualTo("jane.doe@example.com");
        assertThat(response.message()).isEqualTo("User registered successfully");

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(1)).save(userCaptor.capture());
        User capturedUser = userCaptor.getValue();
        assertThat(capturedUser.getPassword()).isEqualTo("$2a$10$encodedHashString");
        assertThat(capturedUser.getRole()).isEqualTo(Role.ROLE_CUSTOMER);
    }

    @Test
    @DisplayName("Should throw UserAlreadyExistsException when email is already registered")
    void register_DuplicateEmail_ThrowsException() {
        // Given
        when(userRepository.existsByEmail("jane.doe@example.com")).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> authService.register(validRequest))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessageContaining("jane.doe@example.com");

        verify(userRepository, never()).save(any(User.class));
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    @DisplayName("Should authenticate user and return LoginResponse with JWT token")
    void login_Success() {
        // Given
        when(userRepository.findByEmail("jane.doe@example.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("SecureP@ss123", "$2a$10$encodedHashString")).thenReturn(true);
        when(jwtService.generateToken(testUser)).thenReturn("mock.jwt.token");
        when(jwtService.getExpirationMs()).thenReturn(86400000L);

        // When
        LoginResponse response = authService.login(validLoginRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.userId()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("Jane Doe");
        assertThat(response.email()).isEqualTo("jane.doe@example.com");
        assertThat(response.role()).isEqualTo("ROLE_CUSTOMER");
        assertThat(response.tokenType()).isEqualTo("Bearer");
        assertThat(response.accessToken()).isEqualTo("mock.jwt.token");
        assertThat(response.expiresIn()).isEqualTo(86400000L);
        verify(jwtService, times(1)).generateToken(testUser);
    }

    @Test
    @DisplayName("Should throw BadCredentialsException when email is not found")
    void login_UserNotFound_ThrowsBadCredentialsException() {
        // Given
        when(userRepository.findByEmail("jane.doe@example.com")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> authService.login(validLoginRequest))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Invalid email or password");

        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(jwtService, never()).generateToken(any(User.class));
    }

    @Test
    @DisplayName("Should throw BadCredentialsException when password does not match")
    void login_InvalidPassword_ThrowsBadCredentialsException() {
        // Given
        when(userRepository.findByEmail("jane.doe@example.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("SecureP@ss123", "$2a$10$encodedHashString")).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> authService.login(validLoginRequest))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Invalid email or password");

        verify(jwtService, never()).generateToken(any(User.class));
    }
}
