package com.skillgapai.model;

/**
 * The outcome of ONE ATS compatibility check (e.g. "does this resume
 * have a Skills heading?"). Mirrors MatchResult's role for skill
 * matching: the "why" behind the check, not just pass/fail.
 *
 * fixSuggestion is null for a PASSED check - there's nothing to fix.
 */
public class AtsCheckResult {

    private final String title;
    private final String description;
    private final String fixSuggestion;
    private final AtsSeverity severity;

    public AtsCheckResult(String title, String description, String fixSuggestion, AtsSeverity severity) {
        this.title = title;
        this.description = description;
        this.fixSuggestion = fixSuggestion;
        this.severity = severity;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getFixSuggestion() {
        return fixSuggestion;
    }

    public AtsSeverity getSeverity() {
        return severity;
    }
}
