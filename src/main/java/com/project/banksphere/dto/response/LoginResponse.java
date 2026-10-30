package com.project.banksphere.dto.response;

import java.time.LocalDateTime;

/**
 * Immutable response carrier returned upon successful authentication.
 * Delivers the signed JWT bearer token along with user identity and role.
 */
public record LoginResponse(
        Long userId,
        String name,
        String email,
        String role,
        String tokenType,
        String accessToken,
        long expiresIn,
        LocalDateTime issuedAt
) {
}
