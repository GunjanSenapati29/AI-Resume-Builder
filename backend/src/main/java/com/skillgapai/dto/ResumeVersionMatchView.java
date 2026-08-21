package com.skillgapai.dto;

import com.skillgapai.model.MatchResult;

import java.util.List;

/**
 * Phase 23: one resume version's match result within a comparison -
 * same matched/missing/underemphasized/matchPercentage shape
 * GapAnalysisResult already produces, just carrying the resume
 * version's id/title alongside it instead of a GapReport's saved id.
 */
public record ResumeVersionMatchView(
        Long resumeVersionId,
        String title,
        List<MatchResult> matched,
        List<MatchResult> missing,
        List<MatchResult> underemphasized,
        double matchPercentage) {
}
