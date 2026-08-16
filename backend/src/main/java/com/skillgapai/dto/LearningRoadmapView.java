package com.skillgapai.dto;

import java.util.List;

/**
 * Phase 17: JSON shape of GET /api/reports/latest/learning-roadmap - the
 * missing and underemphasized skills from the user's MOST RECENT
 * GapReport only, each turned into a SkillRoadmapItemView. Missing
 * skills are ordered Critical -> Important -> Optional; underemphasized
 * skills keep the order the original report returned them in.
 */
public record LearningRoadmapView(
        Long reportId,
        List<SkillRoadmapItemView> missingSkills,
        List<SkillRoadmapItemView> underemphasizedSkills) {
}
