package com.skillgapai.dto;

import com.skillgapai.model.PriorityTier;

import java.util.List;

/**
 * Phase 17: one skill's entry in the Learning Roadmap - the same fixed
 * 4-step template for every skill (see LearningRoadmapService), plus an
 * optional official-documentation link for the small fixed list of
 * skills that have one. priorityTier is only ever set for a MISSING
 * skill (from Phase 15's SkillGapPriority) - underemphasized skills have
 * no priority tier in this data model, so it's null for them.
 */
public record SkillRoadmapItemView(
        String skillName,
        PriorityTier priorityTier,
        List<String> steps,
        String officialDocUrl) {
}
