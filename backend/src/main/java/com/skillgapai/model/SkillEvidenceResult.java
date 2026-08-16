package com.skillgapai.model;

/**
 * The outcome of checking ONE matched skill's evidence across the
 * resume's Skills/Projects/Experience sections. Mirrors MatchResult's
 * role for skill matching - the "why" behind the Strong/Moderate/Weak/
 * No Evidence label, not just the label on its own.
 */
public class SkillEvidenceResult {

    private final String skillName;
    private final boolean inSkillsSection;
    private final boolean inProjectsSection;
    private final boolean inExperienceSection;
    private final EvidenceLevel evidenceLevel;

    public SkillEvidenceResult(String skillName, boolean inSkillsSection, boolean inProjectsSection,
                                boolean inExperienceSection, EvidenceLevel evidenceLevel) {
        this.skillName = skillName;
        this.inSkillsSection = inSkillsSection;
        this.inProjectsSection = inProjectsSection;
        this.inExperienceSection = inExperienceSection;
        this.evidenceLevel = evidenceLevel;
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
