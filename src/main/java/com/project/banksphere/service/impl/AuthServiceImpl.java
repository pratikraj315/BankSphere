package com.project.banksphere.service.impl;

import com.project.banksphere.dto.request.LoginRequest;
import com.project.banksphere.dto.request.RegisterRequest;
import com.project.banksphere.dto.response.LoginResponse;
import com.project.banksphere.dto.response.RegisterResponse;
import com.project.banksphere.entity.User;
import com.project.banksphere.enums.Role;
import com.project.banksphere.exception.UserAlreadyExistsException;
import com.project.banksphere.repository.UserRepository;
import com.project.banksphere.security.JwtService;
import com.project.banksphere.service.AuthService;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Override
    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new UserAlreadyExistsException("A user with email '" + request.email() + "' is already registered");
        }

        User user = User.builder()
                .name(request.name().trim())
                .email(request.email().trim().toLowerCase())
                .mobile(request.mobile().trim())
                .password(passwordEncoder.encode(request.password()))
                .role(Role.ROLE_CUSTOMER)
                .build();

        User savedUser = userRepository.save(user);

        return new RegisterResponse(
                savedUser.getId(),
                savedUser.getName(),
                savedUser.getEmail(),
                "User registered successfully",
                savedUser.getCreatedAt()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        String normalizedEmail = request.email().trim().toLowerCase();

        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BadCredentialsException("Invalid email or password");
        }

        String token = jwtService.generateToken(user);

        return new LoginResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole().name(),
                "Bearer",
                token,
                jwtService.getExpirationMs(),
                LocalDateTime.now()
        );
    }
}
