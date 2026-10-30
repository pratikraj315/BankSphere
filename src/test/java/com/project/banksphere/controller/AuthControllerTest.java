package com.project.banksphere.controller;

import com.project.banksphere.dto.request.RegisterRequest;
import com.project.banksphere.dto.response.RegisterResponse;
import com.project.banksphere.exception.GlobalExceptionHandler;
import com.project.banksphere.exception.UserAlreadyExistsException;
import com.project.banksphere.service.AuthService;

import tools.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

        private MockMvc mockMvc;

        private final ObjectMapper objectMapper = new ObjectMapper();

        @Mock
        private AuthService authService;

        @InjectMocks
        private AuthController authController;

        @BeforeEach
        void setUp() {
                mockMvc = MockMvcBuilders.standaloneSetup(authController)
                                .setControllerAdvice(new GlobalExceptionHandler())
                                .build();
        }

        @Test
        @DisplayName("POST /api/auth/register should return 201 Created on valid request")
        void register_ValidRequest_Returns201Created() throws Exception {
                RegisterRequest request = new RegisterRequest(
                                "Alice Smith",
                                "alice@example.com",
                                "9876543210",
                                "StrongPassword123");

                RegisterResponse mockResponse = new RegisterResponse(
                                10L,
                                "Alice Smith",
                                "alice@example.com",
                                "User registered successfully",
                                LocalDateTime.now());

                when(authService.register(any(RegisterRequest.class))).thenReturn(mockResponse);

                mockMvc.perform(post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.userId").value(10))
                                .andExpect(jsonPath("$.name").value("Alice Smith"))
                                .andExpect(jsonPath("$.email").value("alice@example.com"))
                                .andExpect(jsonPath("$.message").value("User registered successfully"));
        }

        @Test
        @DisplayName("POST /api/auth/register should return 400 Bad Request when validation fails")
        void register_InvalidEmailAndShortPassword_Returns400BadRequest() throws Exception {
                RegisterRequest invalidRequest = new RegisterRequest(
                                "", // Blank name
                                "invalid-email", // Invalid email format
                                "123", // Invalid mobile
                                "short" // Password < 8 characters
                );

                mockMvc.perform(post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(invalidRequest)))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.status").value(400))
                                .andExpect(jsonPath("$.error").value("Bad Request"))
                                .andExpect(jsonPath("$.validationErrors.name").exists())
                                .andExpect(jsonPath("$.validationErrors.email").exists())
                                .andExpect(jsonPath("$.validationErrors.mobile").exists())
                                .andExpect(jsonPath("$.validationErrors.password").exists());
        }

        @Test
        @DisplayName("POST /api/auth/register should return 409 Conflict when user already exists")
        void register_DuplicateEmail_Returns409Conflict() throws Exception {
                RegisterRequest request = new RegisterRequest(
                                "Alice Smith",
                                "alice@example.com",
                                "9876543210",
                                "StrongPassword123");

                when(authService.register(any(RegisterRequest.class)))
                                .thenThrow(new UserAlreadyExistsException(
                                                "A user with email 'alice@example.com' is already registered"));

                mockMvc.perform(post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isConflict())
                                .andExpect(jsonPath("$.status").value(409))
                                .andExpect(jsonPath("$.error").value("Conflict"))
                                .andExpect(jsonPath("$.message")
                                                .value("A user with email 'alice@example.com' is already registered"));
        }
}
