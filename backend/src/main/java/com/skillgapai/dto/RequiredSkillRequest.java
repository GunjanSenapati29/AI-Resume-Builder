package com.skillgapai.dto;

/**
 * JSON shape of one required skill in a POST /api/match request body.
 * Deliberately separate from the Phase 1 {@code RequiredSkill} domain
 * class - this is the API's input contract, not the matching logic's
 * internal model, so the two are free to diverge later.
 */
public record RequiredSkillRequest(String skillName, boolean core) {
}
