package com.skillgapai.dto;

import java.util.List;

/**
 * Phase 20: one role's fit score in the Career Fit view - required
 * skills are split into matched/missing so the score is never shown
 * without the reasoning behind it (matchedSkills already appeared as
 * MATCHED on at least one of the user's GapReports; missingSkills never
 * have).
 */
public record RoleFitView(
        String roleName,
        int score,
        List<String> matchedSkills,
        List<String> missingSkills) {
}
