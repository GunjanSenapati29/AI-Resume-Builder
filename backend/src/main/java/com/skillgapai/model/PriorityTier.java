package com.skillgapai.model;

/**
 * Phase 15: how urgently a missing skill is worth closing, from a
 * combined 0-4 priority score (cross-report recurrence + in-JD
 * emphasis) - see SkillGapPriorityService. 3-4 -> CRITICAL, 1-2 ->
 * IMPORTANT, 0 -> OPTIONAL.
 */
public enum PriorityTier {
    CRITICAL,
    IMPORTANT,
    OPTIONAL
}
