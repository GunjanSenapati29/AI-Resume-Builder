package com.skillgapai.entity;

import com.skillgapai.model.EvidenceLevel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Phase 14: maps to the "skill_evidence" table. One row per skill that
 * was matched on a GapReport, recording which of the three resume
 * section blocks (Skills/Projects/Experience) mentioned it and the
 * resulting Strong/Moderate/Weak/No Evidence classification - see
 * SkillEvidenceService.
 */
@Entity
@Table(name = "skill_evidence")
public class SkillEvidence {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "skill_evidence_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "report_id", nullable = false)
    private GapReport report;

    @Column(name = "skill_name", nullable = false)
    private String skillName;

    @Column(name = "in_skills_section", nullable = false)
    private boolean inSkillsSection;

    @Column(name = "in_projects_section", nullable = false)
    private boolean inProjectsSection;

    @Column(name = "in_experience_section", nullable = false)
    private boolean inExperienceSection;

    @Enumerated(EnumType.STRING)
    @Column(name = "evidence_level", nullable = false, length = 15)
    private EvidenceLevel evidenceLevel;

    protected SkillEvidence() {
        // required by JPA
    }

    public SkillEvidence(GapReport report, String skillName, boolean inSkillsSection,
                          boolean inProjectsSection, boolean inExperienceSection,
                          EvidenceLevel evidenceLevel) {
        this.report = report;
        this.skillName = skillName;
        this.inSkillsSection = inSkillsSection;
        this.inProjectsSection = inProjectsSection;
        this.inExperienceSection = inExperienceSection;
        this.evidenceLevel = evidenceLevel;
    }

    public Long getId() {
        return id;
    }

    public GapReport getReport() {
        return report;
    }

    public String getSkillName() {
        return skillName;
    }

    public boolean isInSkillsSection() {
        return inSkillsSection;
    }

    public boolean isInProjectsSection() {
        return inProjectsSection;
    }

    public boolean isInExperienceSection() {
        return inExperienceSection;
    }

    public EvidenceLevel getEvidenceLevel() {
        return evidenceLevel;
    }
}
