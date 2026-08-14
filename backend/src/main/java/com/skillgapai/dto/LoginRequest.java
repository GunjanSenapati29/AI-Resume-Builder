package com.skillgapai.dto;

/**
 * JSON body of a POST /api/auth/login request.
 */
public record LoginRequest(String email, String password) {
}
