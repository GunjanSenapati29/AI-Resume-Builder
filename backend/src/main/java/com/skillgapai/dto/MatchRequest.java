package com.skillgapai.dto;

import java.util.List;

/**
 * JSON body of a POST /api/match request: the resume text to check
 * (as if already extracted from a PDF - real PDF parsing is Phase 5),
 * the raw pasted job description text (stored on the GapReport row for
 * the record - matching itself still runs off requiredSkills, not this),
 * and the list of skills the job description requires.
 */
public record MatchRequest(String resumeText, String jdText, List<RequiredSkillRequest> requiredSkills) {
}
