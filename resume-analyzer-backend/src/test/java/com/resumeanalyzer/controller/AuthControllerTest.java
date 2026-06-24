package com.resumeanalyzer.controller;

import com.resumeanalyzer.model.dto.AuthResponse;
import com.resumeanalyzer.model.dto.LoginRequest;
import com.resumeanalyzer.model.dto.RegisterRequest;
import com.resumeanalyzer.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuthControllerTest {

    @Test
    void registerShouldReturnCreatedResponseEnvelope() {
        RegisterRequest request = new RegisterRequest("Jane Doe", "jane@example.com", "password123");
        AuthResponse authResponse = AuthResponse.of(
                UUID.fromString("123e4567-e89b-12d3-a456-426614174020"),
                "Jane Doe",
                "jane@example.com",
                "token-value"
        );
        AuthController controller = new AuthController(new StubAuthService(authResponse, null));

        ResponseEntity<?> response = controller.register(request);

        assertEquals(201, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof com.resumeanalyzer.model.dto.ApiResponse);
        @SuppressWarnings("unchecked")
        com.resumeanalyzer.model.dto.ApiResponse<AuthResponse> body = (com.resumeanalyzer.model.dto.ApiResponse<AuthResponse>) response.getBody();
        assertTrue(body.success());
        assertEquals(authResponse, body.data());
        assertEquals("Account created successfully", body.message());
    }

    @Test
    void loginShouldReturnOkResponseEnvelope() {
        LoginRequest request = new LoginRequest("jane@example.com", "password123");
        AuthResponse authResponse = AuthResponse.of(
                UUID.fromString("123e4567-e89b-12d3-a456-426614174021"),
                "Jane Doe",
                "jane@example.com",
                "token-value"
        );
        AuthController controller = new AuthController(new StubAuthService(null, authResponse));

        ResponseEntity<?> response = controller.login(request);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof com.resumeanalyzer.model.dto.ApiResponse);
        @SuppressWarnings("unchecked")
        com.resumeanalyzer.model.dto.ApiResponse<AuthResponse> body = (com.resumeanalyzer.model.dto.ApiResponse<AuthResponse>) response.getBody();
        assertTrue(body.success());
        assertEquals(authResponse, body.data());
        assertEquals("Login successful", body.message());
    }

    private static final class StubAuthService extends AuthService {

        private final AuthResponse registerResponse;
        private final AuthResponse loginResponse;

        private StubAuthService(AuthResponse registerResponse, AuthResponse loginResponse) {
            super(null, null, null);
            this.registerResponse = registerResponse;
            this.loginResponse = loginResponse;
        }

        @Override
        public AuthResponse register(RegisterRequest request) {
            return registerResponse;
        }

        @Override
        public AuthResponse login(LoginRequest request) {
            return loginResponse;
        }
    }
}