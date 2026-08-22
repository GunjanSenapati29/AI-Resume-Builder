package com.skillgapai.dto;

import com.skillgapai.model.JobReadinessLabel;

import java.time.LocalDateTime;

/**
 * Phase 24: JSON shape of GET /api/reports/dashboard-summary - the Job
 * Readiness Score/label from the user's single most recent report, plus
 * how many reports they've ever run and when the latest one was. Pure
 * aggregation of columns GapReport already stores - no new scoring logic.
 */
public record DashboardSummaryView(
        int jobReadinessScore,
        JobReadinessLabel jobReadinessLabel,
        long totalReports,
        LocalDateTime lastAnalyzedAt) {
}
