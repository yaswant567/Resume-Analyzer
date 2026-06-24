package com.resumeanalyzer.service;

import com.resumeanalyzer.exception.CustomExceptions.EmailAlreadyExistsException;
import com.resumeanalyzer.exception.CustomExceptions.InvalidCredentialsException;
import com.resumeanalyzer.model.dto.AuthResponse;
import com.resumeanalyzer.model.dto.LoginRequest;
import com.resumeanalyzer.model.dto.RegisterRequest;
import com.resumeanalyzer.model.entity.User;
import com.resumeanalyzer.repository.UserRepository;
import com.resumeanalyzer.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;


    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException(request.email());
        }

        User user = User.builder()
                .name(request.name())
                .email(request.email().toLowerCase())
                .password(passwordEncoder.encode(request.password()))
                .dailyAnalysisCount(0)
                .build();

        User saved = userRepository.save(user);
        String token = jwtUtil.generateToken(saved.getId(), saved.getEmail());

        return AuthResponse.of(saved.getId(), saved.getName(), saved.getEmail(), token);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email().toLowerCase())
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new InvalidCredentialsException();
        }

        String token = jwtUtil.generateToken(user.getId(), user.getEmail());

        return AuthResponse.of(user.getId(), user.getName(), user.getEmail(), token);
    }
}
