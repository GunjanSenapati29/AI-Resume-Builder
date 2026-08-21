package com.skillgapai.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Phase 22: full JSON shape returned by create/get/update/duplicate -
 * every section reconstructed from the entity's JSON columns, in the
 * same structured shape ResumeVersionRequest sends it in.
 */
public record ResumeVersionView(
        Long id,
        String title,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        ContactInfo contact,
        String summary,
        List<String> skills,
        List<ProjectEntry> projects,
        List<EducationEntry> education,
        List<ExperienceEntry> experience,
        List<CertificationEntry> certifications) {
}
