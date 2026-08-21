package com.skillgapai.dto;

/**
 * Phase 22: the one required section of a ResumeVersion - every other
 * field is optional, but name must be present (see ResumeVersionService).
 */
public record ContactInfo(
        String name,
        String email,
        String phone,
        String location,
        String portfolioUrl,
        String githubUrl,
        String linkedinUrl) {
}
