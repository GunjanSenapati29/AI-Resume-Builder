package com.skillgapai.dto;

import com.skillgapai.model.AtsSeverity;

/**
 * JSON shape of one ATS compatibility check, nested inside
 * GapReportView.atsIssues. fixSuggestion is null for a PASSED check.
 */
public record AtsIssueView(String title, String description, String fixSuggestion, AtsSeverity severity) {
}
