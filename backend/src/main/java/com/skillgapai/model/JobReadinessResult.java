package com.skillgapai.model;

/**
 * The outcome of combining a report's skill match %, ATS score, evidence
 * strength, and gap severity into one composite readiness score. Mirrors
 * AtsAnalysisResult's role for ATS checks - carries the overall score AND
 * every component behind it, never just the final number, since this
 * project never shows a verdict without showing why. See
 * JobReadinessService.
 */
public class JobReadinessResult {

    private final int overallScore;
    private final JobReadinessLabel label;
    private final int skillMatchScore;
    private final int atsScore;
    private final int evidenceStrengthScore;
    private final int gapSeverityScore;

    public JobReadinessResult(int overallScore, JobReadinessLabel label, int skillMatchScore, int atsScore,
                               int evidenceStrengthScore, int gapSeverityScore) {
        this.overallScore = overallScore;
        this.label = label;
        this.skillMatchScore = skillMatchScore;
        this.atsScore = atsScore;
        this.evidenceStrengthScore = evidenceStrengthScore;
        this.gapSeverityScore = gapSeverityScore;
    }

    public int getOverallScore() {
        return overallScore;
    }

    public JobReadinessLabel getLabel() {
        return label;
    }

    public int getSkillMatchScore() {
        return skillMatchScore;
    }

    public int getAtsScore() {
        return atsScore;
    }

    public int getEvidenceStrengthScore() {
        return evidenceStrengthScore;
    }

    public int getGapSeverityScore() {
        return gapSeverityScore;
    }
}
