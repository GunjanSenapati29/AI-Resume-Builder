package com.skillgapai.service;

import com.skillgapai.dto.AuthResponse;
import com.skillgapai.dto.LoginRequest;
import com.skillgapai.dto.SignupRequest;
import com.skillgapai.entity.User;
import com.skillgapai.repository.UserRepository;
import com.skillgapai.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Phase 6e: signup hashes the password with BCrypt (never store or
 * compare the raw password) and creates the User row; login looks the
 * user up by email and checks the submitted password against the
 * stored hash. Both return a fresh JWT on success - there's no separate
 * "am I logged in" endpoint, the frontend just holds onto the token.
 */
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public boolean emailExists(String email) {
        return userRepository.findByEmail(email).isPresent();
    }

    public AuthResponse signup(SignupRequest request) {
        User user = userRepository.save(
                new User(request.name(), request.email(), passwordEncoder.encode(request.password())));
        return new AuthResponse(jwtService.generateToken(user.getEmail()), user.getName(), user.getEmail());
    }

    public Optional<AuthResponse> login(LoginRequest request) {
        return userRepository.findByEmail(request.email())
                .filter(user -> passwordEncoder.matches(request.password(), user.getPasswordHash()))
                .map(user -> new AuthResponse(jwtService.generateToken(user.getEmail()), user.getName(), user.getEmail()));
    }
}
