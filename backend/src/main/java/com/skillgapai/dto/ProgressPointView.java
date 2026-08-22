package com.skillgapai.dto;

import com.skillgapai.model.JobReadinessLabel;

import java.time.LocalDateTime;

/**
 * Phase 24: one point in GET /api/reports/progress-trend - one row per
 * report, sorted chronologically oldest to newest (reverse of the usual
 * newest-first convention) so the frontend can plot it left to right
 * without re-sorting.
 */
public record ProgressPointView(
        Long reportId,
        LocalDateTime createdAt,
        int jobReadinessScore,
        JobReadinessLabel jobReadinessLabel) {
}
