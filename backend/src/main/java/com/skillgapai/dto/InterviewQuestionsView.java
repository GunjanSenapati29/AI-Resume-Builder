package com.skillgapai.dto;

import java.util.List;

/**
 * Phase 19: JSON shape of GET /api/reports/latest/interview-questions -
 * one entry per MATCHED skill on the user's MOST RECENT GapReport (not
 * missing/underemphasized - those are skills the user doesn't actually
 * claim, which isn't what an interviewer would probe).
 */
public record InterviewQuestionsView(
        Long reportId,
        List<SkillInterviewQuestionsView> skills) {
}
