package com.project.banksphere.dto.response;

import java.time.LocalDateTime;

/**
 * Immutable response carrier returned upon successful user registration.
 * Never leaks sensitive data such as password hashes.
 */
public record RegisterResponse(
        Long userId,
        String name,
        String email,
        String message,
        LocalDateTime registeredAt
) {
}
