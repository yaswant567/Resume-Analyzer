package com.resumeanalyzer.model.dto;

import java.util.UUID;

public record AuthResponse(
        UUID userId,
        String name,
        String email,
        String token,
        String tokenType
) {
    public static AuthResponse of(UUID userId, String name, String email, String token) {
        return new AuthResponse(userId, name, email, token, "Bearer");
    }
}
