package com.skillgapai.dto;

import com.skillgapai.model.EvidenceLevel;

/**
 * JSON shape of one matched skill's evidence classification, nested
 * inside GapReportView.skillEvidence - see SkillEvidenceService.
 */
public record SkillEvidenceView(String skillName, boolean inSkillsSection, boolean inProjectsSection,
                                 boolean inExperienceSection, EvidenceLevel evidenceLevel) {
}
