package com.skillgapai.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Phase 21: one report's row in GET /api/reports/compare - deliberately
 * just a reshaping of data already stored on GapReport (label/matched/
 * missing), no new scoring logic. `label` is a short (60-char) preview
 * of the stored jd_text, same truncate-with-ellipsis approach
 * GapReportSummary's jdSnippet already uses at a different length.
 */
public record ReportComparisonItemView(
        Long reportId,
        String label,
        LocalDateTime createdAt,
        double matchPercentage,
        List<String> matchedSkills,
        List<String> missingSkills) {
}
