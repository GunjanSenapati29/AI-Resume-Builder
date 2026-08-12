package com.skillgapai.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    protected GapReport() {
        // required by JPA
    }

    public GapReport(Resume resume, String jdText, String matchedSkillsJson,
                      String missingSkillsJson, String underemphasizedSkillsJson,
                      double matchPercentage, LocalDateTime createdAt) {
        this.resume = resume;
        this.jdText = jdText;
        this.matchedSkillsJson = matchedSkillsJson;
        this.missingSkillsJson = missingSkillsJson;
        this.underemphasizedSkillsJson = underemphasizedSkillsJson;
        this.matchPercentage = matchPercentage;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
