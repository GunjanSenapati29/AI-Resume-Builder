package com.skillgapai.dto;

/**
 * JSON shape returned by both POST /api/auth/signup and
 * POST /api/auth/login - the frontend stores the token and attaches it
 * as "Authorization: Bearer <token>" on every later request.
 */
public record AuthResponse(String token, String name, String email) {
}
