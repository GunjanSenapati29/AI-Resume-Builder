package com.skillgapai.model;

/**
 * The outcome of one ATS compatibility check. PASSED isn't a lesser
 * severity than MEDIUM/HIGH - it just means the check found nothing to
 * fix, and still gets recorded so the report can show every check that
 * ran, not only the ones that failed (same "visible reasoning" principle
 * as MatchType for skill matching).
 */
public enum AtsSeverity {
    PASSED,
    MEDIUM,
    HIGH
}
