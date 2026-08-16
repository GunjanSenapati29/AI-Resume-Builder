package com.skillgapai.model;

/**
 * Phase 14: the resume's text sliced into its Skills/Experience/Projects
 * section blocks, as found by AtsAnalyzerService.extractSectionBlocks().
 * A section that wasn't found in the resume is an empty string, not null.
 */
public record ResumeSectionBlocks(String skillsText, String experienceText, String projectsText) {
}
