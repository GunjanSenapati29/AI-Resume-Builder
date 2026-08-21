package com.skillgapai.dto;

import java.util.List;

/**
 * Phase 23: JSON shape of POST /api/resume-versions/compare - jdText is
 * echoed back only for display context (what was this compared
 * against); nothing here is persisted, this is computed live on every
 * call. results is sorted by matchPercentage descending, same
 * best-fit-first convention Phase 21's compareReports uses.
 */
public record ResumeVersionComparisonView(String jdText, List<ResumeVersionMatchView> results) {
}
