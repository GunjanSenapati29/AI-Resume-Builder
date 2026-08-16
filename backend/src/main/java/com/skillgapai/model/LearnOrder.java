package com.skillgapai.model;

/**
 * Phase 15: maps 1:1 from PriorityTier (CRITICAL -> LEARN_FIRST,
 * IMPORTANT -> LEARN_NEXT, OPTIONAL -> LEARN_LATER) - see
 * SkillGapPriorityService. Kept as its own field/enum rather than
 * derived in the view layer, since it's what the UI actually labels
 * each missing skill with.
 */
public enum LearnOrder {
    LEARN_FIRST,
    LEARN_NEXT,
    LEARN_LATER
}
