package com.skillgapai.dto;

import java.time.LocalDateTime;

/**
 * JSON shape of one row in GET /api/reports - just enough for the
 * History screen's list view (Phase 6d). jdSnippet is a truncated
 * preview of the stored jd_text so the list doesn't have to ship the
 * full matched/missing/underemphasized payload for every past report.
 */
public record GapReportSummary(Long id, String jdSnippet, double matchPercentage, LocalDateTime createdAt) {
}
