package com.resumeanalyzer.security;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

class JwtUtilTest {

    private static final String SECRET = "0123456789abcdef0123456789abcdef";

    private final JwtUtil jwtUtil = new JwtUtil(SECRET, 60_000);

    @Test
    void generateTokenShouldRoundTripClaims() {
        UUID userId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        String email = "user@example.com";

        String token = jwtUtil.generateToken(userId, email);

        assertTrue(jwtUtil.isTokenValid(token));
        assertEquals(userId, jwtUtil.extractUserId(token));
        assertEquals(email, jwtUtil.extractEmail(token));
    }

    @Test
    void invalidTokenShouldBeRejected() {
        assertFalse(jwtUtil.isTokenValid("not-a-jwt"));
    }

    @Test
    void expiredTokenShouldBeRejected() {
        JwtUtil expiredJwtUtil = new JwtUtil(SECRET, -1L);
        String token = expiredJwtUtil.generateToken(UUID.fromString("123e4567-e89b-12d3-a456-426614174001"), "expired@example.com");

        assertFalse(expiredJwtUtil.isTokenValid(token));
    }
}