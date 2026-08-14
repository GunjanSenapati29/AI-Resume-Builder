package com.skillgapai.dto;

import java.util.List;

/**
 * JSON body of a POST /api/match request: the resume text to check
 * (as if already extracted from a PDF - real PDF parsing is Phase 5)
 * plus the list of skills the job description requires.
 */
public record MatchRequest(String resumeText, List<RequiredSkillRequest> requiredSkills) {
}
