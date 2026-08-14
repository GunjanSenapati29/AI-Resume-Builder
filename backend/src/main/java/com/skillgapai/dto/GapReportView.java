package com.skillgapai.dto;

import tools.jackson.databind.JsonNode;

import java.time.LocalDateTime;

/**
 * JSON shape returned by both POST /api/match and GET /api/reports/{id} -
 * the same view whether a report was just created or looked up later.
 *
 * matched/missing/underemphasized are JsonNode (Jackson's generic JSON
 * tree type) rather than List<MatchResult>: a freshly-created report
 * builds this from the in-memory GapAnalysisResult, while GET rebuilds
 * it by reading the matched_skills_json/etc. columns back out of MySQL -
 * JsonNode lets both paths produce identical output without needing
 * GapReport's stored JSON to round-trip through a Java object first.
 */
public record GapReportView(
        Long id,
        Long resumeId,
        String jdText,
        JsonNode matched,
        JsonNode missing,
        JsonNode underemphasized,
        double matchPercentage,
        LocalDateTime createdAt) {
}
