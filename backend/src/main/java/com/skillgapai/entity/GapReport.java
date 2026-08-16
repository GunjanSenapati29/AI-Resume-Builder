package com.skillgapai.entity;

import com.skillgapai.model.JobReadinessLabel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * Maps to the "gap_reports" table. One row per completed match run
 * (resume vs. a specific pasted JD), storing the matched/missing/
 * underemphasized skill lists as JSON text so we don't need extra
 * join tables for this first version.
 *
 * Phase 13: also carries the ATS compatibility score for the same run -
 * the per-check breakdown behind that score lives in the separate
 * ats_issues table (see AtsIssue), joined back to this row by report_id.
 *
 * Phase 16: also carries the Job Readiness Score for the same run.
 * skillMatchScore and atsScore aren't duplicated here - the composite
 * reads them straight from matchPercentage/atsScore above - but
 * evidenceStrengthScore and gapSeverityScore are derived from the
 * separate skill_evidence/skill_gap_priorities tables, so they're stored
 * here rather than recomputed on every read. Same DB-level-default
 * pattern as ats_score: reports created before this phase existed just
 * read back the default (0 / NOT_READY), same as ats_score reads back 0
 * for reports created before Phase 13.
 */
@Entity
@Table(name = "gap_reports")
public class GapReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "report_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_id", nullable = false)
    private Resume resume;

    @Lob
    @Column(name = "jd_text", nullable = false, columnDefinition = "LONGTEXT")
    private String jdText;

    @Lob
    @Column(name = "matched_skills_json", nullable = false, columnDefinition = "LONGTEXT")
    private String matchedSkillsJson;

    @Lob
    @Column(name = "missing_skills_json", nullable = false, columnDefinition = "LONGTEXT")
    private String missingSkillsJson;

    @Lob
    @Column(name = "underemphasized_skills_json", nullable = false, columnDefinition = "LONGTEXT")
    private String underemphasizedSkillsJson;

    @Column(name = "match_percentage", nullable = false)
    private double matchPercentage;

    // Phase 13: added to an existing table that may already have rows,
    // so this needs a DB-level default (not just nullable=false) - a
    // plain NOT NULL with no default would fail the ALTER TABLE against
    // any row created before this column existed.
    @Column(name = "ats_score", nullable = false, columnDefinition = "INTEGER NOT NULL DEFAULT 0")
    private int atsScore;

    // Phase 16: same ALTER-TABLE-safe-default reasoning as ats_score above.
    @Column(name = "job_readiness_score", nullable = false, columnDefinition = "INTEGER NOT NULL DEFAULT 0")
    private int jobReadinessScore;

    @Enumerated(EnumType.STRING)
    @Column(name = "job_readiness_label", nullable = false, length = 15,
            columnDefinition = "VARCHAR(15) NOT NULL DEFAULT 'NOT_READY'")
    private JobReadinessLabel jobReadinessLabel;

    @Column(name = "evidence_strength_score", nullable = false, columnDefinition = "INTEGER NOT NULL DEFAULT 0")
    private int evidenceStrengthScore;

    @Column(name = "gap_severity_score", nullable = false, columnDefinition = "INTEGER NOT NULL DEFAULT 0")
    private int gapSeverityScore;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    protected GapReport() {
        // required by JPA
    }

    public GapReport(Resume resume, String jdText, String matchedSkillsJson,
                      String missingSkillsJson, String underemphasizedSkillsJson,
                      double matchPercentage, int atsScore, int jobReadinessScore,
                      JobReadinessLabel jobReadinessLabel, int evidenceStrengthScore,
                      int gapSeverityScore, LocalDateTime createdAt) {
        this.resume = resume;
        this.jdText = jdText;
        this.matchedSkillsJson = matchedSkillsJson;
        this.missingSkillsJson = missingSkillsJson;
        this.underemphasizedSkillsJson = underemphasizedSkillsJson;
        this.matchPercentage = matchPercentage;
        this.atsScore = atsScore;
        this.jobReadinessScore = jobReadinessScore;
        this.jobReadinessLabel = jobReadinessLabel;
        this.evidenceStrengthScore = evidenceStrengthScore;
        this.gapSeverityScore = gapSeverityScore;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public Resume getResume() {
        return resume;
    }

    public String getJdText() {
        return jdText;
    }

    public String getMatchedSkillsJson() {
        return matchedSkillsJson;
    }

    public String getMissingSkillsJson() {
        return missingSkillsJson;
    }

    public String getUnderemphasizedSkillsJson() {
        return underemphasizedSkillsJson;
    }

    public double getMatchPercentage() {
        return matchPercentage;
    }

    public int getAtsScore() {
        return atsScore;
    }

    public int getJobReadinessScore() {
        return jobReadinessScore;
    }

    public JobReadinessLabel getJobReadinessLabel() {
        return jobReadinessLabel;
    }

    public int getEvidenceStrengthScore() {
        return evidenceStrengthScore;
    }

    public int getGapSeverityScore() {
        return gapSeverityScore;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
