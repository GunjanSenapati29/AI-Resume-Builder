package com.skillgapai.dto;

import com.skillgapai.model.JobReadinessLabel;

/**
 * Phase 16: the Job Readiness Score section of the Gap Report - the
 * overall 0-100 score and label, plus all four weighted component scores
 * behind it (Skill Match 40%, ATS Compatibility 20%, Evidence Strength
 * 20%, Gap Severity 20%) - see JobReadinessService. Always shown together
 * so a user can see WHY the score is what it is, not just the number.
 */
public record JobReadinessView(
        int overallScore,
        JobReadinessLabel label,
        int skillMatchScore,
        int atsScore,
        int evidenceStrengthScore,
        int gapSeverityScore) {
}
