package com.skillgapai.model;

/**
 * Phase 16: the plain-language band for the overall Job Readiness Score -
 * see JobReadinessService. 85-100 -> EXCELLENT, 70-84 -> STRONG, 50-69 ->
 * NEEDS_WORK, 0-49 -> NOT_READY.
 */
public enum JobReadinessLabel {
    EXCELLENT,
    STRONG,
    NEEDS_WORK,
    NOT_READY
}
