package com.skillgapai.model;

/**
 * Phase 14: how well a matched skill is backed up across the resume's
 * Skills/Projects/Experience sections - see SkillEvidenceService. Named
 * by count of sections the skill appears in: 3 -> STRONG, 2 -> MODERATE,
 * 1 -> WEAK, 0 -> NO_EVIDENCE (rare for a skill SkillMatchingService
 * already found somewhere in the resume, but possible if that mention
 * fell outside all three detected section blocks).
 */
public enum EvidenceLevel {
    STRONG,
    MODERATE,
    WEAK,
    NO_EVIDENCE
}
