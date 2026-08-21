package com.skillgapai.dto;

import java.util.List;

/**
 * Phase 22: request body for both POST /api/resume-versions and
 * PUT /api/resume-versions/{id}. Every section past contact is optional
 * and may arrive null - ResumeVersionService treats a null list/string
 * the same as an empty one.
 */
public record ResumeVersionRequest(
        String title,
        ContactInfo contact,
        String summary,
        List<String> skills,
        List<ProjectEntry> projects,
        List<EducationEntry> education,
        List<ExperienceEntry> experience,
        List<CertificationEntry> certifications) {
}
