package com.resumeanalyzer.service;

import com.resumeanalyzer.exception.CustomExceptions.EmailAlreadyExistsException;
import com.resumeanalyzer.exception.CustomExceptions.InvalidCredentialsException;
import com.resumeanalyzer.model.dto.AuthResponse;
import com.resumeanalyzer.model.dto.LoginRequest;
import com.resumeanalyzer.model.dto.RegisterRequest;
import com.resumeanalyzer.model.entity.User;
import com.resumeanalyzer.repository.UserRepository;
import com.resumeanalyzer.security.JwtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuthServiceTest {

    private static final String SECRET = "0123456789abcdef0123456789abcdef";

    @Test
    void registerShouldCreateUserAndReturnBearerToken() {
        RegisterRequest request = new RegisterRequest("Jane Doe", "JANE@Example.com", "password123");
        UUID userId = UUID.fromString("123e4567-e89b-12d3-a456-426614174010");

        UserRepositoryStub repositoryStub = new UserRepositoryStub();
        repositoryStub.existsByEmailResult = false;
        repositoryStub.savedUserResult = User.builder()
                .id(userId)
                .name("Jane Doe")
                .email("jane@example.com")
                .password("encoded-password")
                .dailyAnalysisCount(0)
                .build();

        JwtUtil jwtUtil = new JwtUtil(SECRET, 60_000);
        AuthService authService = new AuthService(
                repositoryStub.proxy(),
                new PasswordEncoder() {
                    @Override
                    public String encode(CharSequence rawPassword) {
                        return "encoded-password";
                    }

                    @Override
                    public boolean matches(CharSequence rawPassword, String encodedPassword) {
                        return "password123".contentEquals(rawPassword) && "encoded-password".equals(encodedPassword);
                    }
                },
                jwtUtil
        );

        AuthResponse response = authService.register(request);

        assertEquals(userId, response.userId());
        assertEquals("Jane Doe", response.name());
        assertEquals("jane@example.com", response.email());
        assertEquals("Bearer", response.tokenType());
        assertTrue(jwtUtil.isTokenValid(response.token()));
        assertEquals(userId, jwtUtil.extractUserId(response.token()));
        assertEquals("jane@example.com", jwtUtil.extractEmail(response.token()));

        assertEquals("jane@example.com", repositoryStub.savedUser.getEmail());
        assertEquals("encoded-password", repositoryStub.savedUser.getPassword());
        assertEquals(0, repositoryStub.savedUser.getDailyAnalysisCount());
    }

    @Test
    void registerShouldRejectExistingEmail() {
        RegisterRequest request = new RegisterRequest("Jane Doe", "jane@example.com", "password123");

        UserRepositoryStub repositoryStub = new UserRepositoryStub();
        repositoryStub.existsByEmailResult = true;

        AuthService authService = new AuthService(
                repositoryStub.proxy(),
                new PasswordEncoder() {
                    @Override
                    public String encode(CharSequence rawPassword) {
                        return rawPassword.toString();
                    }

                    @Override
                    public boolean matches(CharSequence rawPassword, String encodedPassword) {
                        return false;
                    }
                },
                new JwtUtil(SECRET, 60_000)
        );

        EmailAlreadyExistsException exception = assertThrows(EmailAlreadyExistsException.class, () -> authService.register(request));

        assertTrue(exception.getMessage().contains("jane@example.com"));
        assertFalse(repositoryStub.saveCalled);
    }

    @Test
    void loginShouldReturnTokenForValidCredentials() {
        LoginRequest request = new LoginRequest("JANE@Example.com", "password123");
        UUID userId = UUID.fromString("123e4567-e89b-12d3-a456-426614174011");

        UserRepositoryStub repositoryStub = new UserRepositoryStub();
        repositoryStub.findByEmailResult = Optional.of(User.builder()
                .id(userId)
                .name("Jane Doe")
                .email("jane@example.com")
                .password("encoded-password")
                .build());

        JwtUtil jwtUtil = new JwtUtil(SECRET, 60_000);
        AuthService authService = new AuthService(
                repositoryStub.proxy(),
                new PasswordEncoder() {
                    @Override
                    public String encode(CharSequence rawPassword) {
                        return "encoded-password";
                    }

                    @Override
                    public boolean matches(CharSequence rawPassword, String encodedPassword) {
                        return "password123".contentEquals(rawPassword) && "encoded-password".equals(encodedPassword);
                    }
                },
                jwtUtil
        );

        AuthResponse response = authService.login(request);

        assertEquals(userId, response.userId());
        assertEquals("Jane Doe", response.name());
        assertEquals("jane@example.com", response.email());
        assertEquals("Bearer", response.tokenType());
        assertTrue(jwtUtil.isTokenValid(response.token()));
        assertEquals(userId, jwtUtil.extractUserId(response.token()));
        assertEquals("jane@example.com", jwtUtil.extractEmail(response.token()));
    }

    @Test
    void loginShouldRejectInvalidPassword() {
        LoginRequest request = new LoginRequest("jane@example.com", "wrong-password");

        UserRepositoryStub repositoryStub = new UserRepositoryStub();
        repositoryStub.findByEmailResult = Optional.of(User.builder()
                .id(UUID.fromString("123e4567-e89b-12d3-a456-426614174012"))
                .name("Jane Doe")
                .email("jane@example.com")
                .password("encoded-password")
                .build());

        AuthService authService = new AuthService(
                repositoryStub.proxy(),
                new PasswordEncoder() {
                    @Override
                    public String encode(CharSequence rawPassword) {
                        return rawPassword.toString();
                    }

                    @Override
                    public boolean matches(CharSequence rawPassword, String encodedPassword) {
                        return false;
                    }
                },
                new JwtUtil(SECRET, 60_000)
        );

        assertThrows(InvalidCredentialsException.class, () -> authService.login(request));
        assertFalse(repositoryStub.saveCalled);
    }

    private static final class UserRepositoryStub implements InvocationHandler {

        private boolean existsByEmailResult;
        private Optional<User> findByEmailResult = Optional.empty();
        private User savedUser;
        private User savedUserResult;
        private boolean saveCalled;

        UserRepository proxy() {
            return (UserRepository) Proxy.newProxyInstance(
                    UserRepository.class.getClassLoader(),
                    new Class<?>[]{UserRepository.class},
                    this
            );
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) {
            String name = method.getName();
            if (name.equals("existsByEmail")) {
                return existsByEmailResult;
            }
            if (name.equals("findByEmail")) {
                return findByEmailResult;
            }
            if (name.equals("save")) {
                saveCalled = true;
                savedUser = (User) args[0];
                return savedUserResult != null ? savedUserResult : args[0];
            }
            if (name.equals("toString")) {
                return "UserRepositoryStub";
            }
            if (name.equals("hashCode")) {
                return System.identityHashCode(proxy);
            }
            if (name.equals("equals")) {
                return proxy == args[0];
            }

            Class<?> returnType = method.getReturnType();
            if (returnType.equals(boolean.class)) {
                return false;
            }
            if (returnType.isPrimitive()) {
                return 0;
            }
            return null;
        }
    }
}