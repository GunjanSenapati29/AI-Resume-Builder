package com.skillgapai.dto;

import java.util.List;

/**
 * Phase 23: JSON body of POST /api/resume-versions/compare - exactly 2
 * of the logged-in user's own resume version ids, checked against the
 * same jdText/requiredSkills a POST /api/match request would use.
 * Reuses RequiredSkillRequest rather than inventing a second shape for
 * "a skill the JD requires".
 */
public record ResumeVersionCompareRequest(
        List<Long> resumeVersionIds,
        String jdText,
        List<RequiredSkillRequest> requiredSkills) {
}
