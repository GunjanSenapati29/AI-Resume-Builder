package com.skillgapai.resumebuilder;

/**
 * Phase 23: one ownership-checked ResumeVersion's title plus its
 * sections flattened to plain text, ready to hand to
 * SkillMatchingService.analyze() - an internal carrier between
 * ResumeVersionService and ResumeVersionComparisonService, not an API
 * response shape.
 */
public record FlattenedResumeVersion(String title, String text) {
}
