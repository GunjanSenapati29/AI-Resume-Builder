package com.skillgapai.dto;

/**
 * JSON body of a POST /api/auth/signup request.
 */
public record SignupRequest(String name, String email, String password) {
}
