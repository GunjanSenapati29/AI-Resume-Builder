package com.skillgapai.dto;

import com.skillgapai.model.Difficulty;

/**
 * JSON shape of one entry in GET /api/skills - just enough for the
 * Upload & Compare screen to render a checklist (Phase 6a), without the
 * frontend hardcoding a duplicate copy of SkillsTaxonomy.sampleTaxonomy().
 */
public record SkillSummaryResponse(String name, Difficulty difficulty) {
}
