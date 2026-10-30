package com.project.banksphere.dto.response;

import java.time.LocalDateTime;

/**
 * Immutable carrier for authenticated user profile information.
 */
public record UserProfileResponse(
        Long userId,
        String name,
        String email,
        String mobile,
        String role,
        LocalDateTime createdAt
) {
}
