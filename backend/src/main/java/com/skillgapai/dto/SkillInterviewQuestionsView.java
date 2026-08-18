package com.skillgapai.dto;

import java.util.List;

/**
 * Phase 19: one matched skill's entry in the Interview Prep page -
 * always exactly 3 questions, either the curated set for that skill
 * (see InterviewQuestionService.CURATED_QUESTIONS) or the generic
 * 3-question fallback with the skill name filled in.
 */
public record SkillInterviewQuestionsView(
        String skillName,
        List<String> questions) {
}
