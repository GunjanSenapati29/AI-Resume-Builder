package com.skillgapai.dto;

import java.time.LocalDateTime;

/** Phase 22: one row in the Resume Builder's list view (GET /api/resume-versions). */
public record ResumeVersionSummary(Long id, String title, LocalDateTime updatedAt) {
}
