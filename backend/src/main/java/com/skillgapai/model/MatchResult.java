package com.skillgapai.model;

/**
 * The outcome of checking ONE required skill against the resume text.
 *
 * This object is what the "reasoning" chip in the UI will eventually be
 * built from (Section 5 of the spec: "never show a result without
 * showing why").
 */
public class MatchResult {

    private final String skillName;
    private final MatchType matchType;
    private final String reason;
    private final int occurrenceCount;
    private final Difficulty difficulty;

    public MatchResult(String skillName, MatchType matchType, String reason,
                        int occurrenceCount, Difficulty difficulty) {
        this.skillName = skillName;
        this.matchType = matchType;
        this.reason = reason;
        this.occurrenceCount = occurrenceCount;
        this.difficulty = difficulty;
    }

    public String getSkillName() {
        return skillName;
    }

    public MatchType getMatchType() {
        return matchType;
    }

    public String getReason() {
        return reason;
    }

    public int getOccurrenceCount() {
        return occurrenceCount;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    @Override
    public String toString() {
        return String.format("%-15s [%-9s] %s", skillName, matchType, reason);
    }
}
