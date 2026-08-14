package com.skillgapai.web;

import com.skillgapai.dto.LoginRequest;
import com.skillgapai.dto.SignupRequest;
import com.skillgapai.service.AuthService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Phase 6e: the only two routes SecurityConfig leaves open to anyone -
 * every other /api/** route needs the JWT one of these hands back.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final int MIN_PASSWORD_LENGTH = 8;

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody SignupRequest request) {
        if (isBlank(request.name())) {
            return ResponseEntity.badRequest().body("name is required.");
        }
        if (isBlank(request.email())) {
            return ResponseEntity.badRequest().body("email is required.");
        }
        if (request.password() == null || request.password().length() < MIN_PASSWORD_LENGTH) {
            return ResponseEntity.badRequest().body("password must be at least " + MIN_PASSWORD_LENGTH + " characters.");
        }
        if (authService.emailExists(request.email())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("An account with this email already exists.");
        }

        try {
            return ResponseEntity.ok(authService.signup(request));
        } catch (DataIntegrityViolationException e) {
            // Two signups for the same email arrived at once and both
            // passed the emailExists() check above - the unique
            // constraint on users.email is the real backstop.
            return ResponseEntity.status(HttpStatus.CONFLICT).body("An account with this email already exists.");
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        if (isBlank(request.email()) || isBlank(request.password())) {
            return ResponseEntity.badRequest().body("email and password are required.");
        }

        return authService.login(request)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid email or password."));
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
