package com.skillgapai.model;

/**
 * The outcome of prioritizing ONE missing skill. Mirrors MatchResult's
 * role for skill matching - the "why" behind the priority tier
 * (crossReportCount, inJdMentionCount), not just the tier on its own.
 */
public class SkillGapPriorityResult {

    private final String skillName;
    private final int crossReportCount;
    private final int inJdMentionCount;
    private final int priorityScore;
    private final PriorityTier priorityTier;
    private final LearnOrder learnOrder;

    public SkillGapPriorityResult(String skillName, int crossReportCount, int inJdMentionCount,
                                   int priorityScore, PriorityTier priorityTier, LearnOrder learnOrder) {
        this.skillName = skillName;
        this.crossReportCount = crossReportCount;
        this.inJdMentionCount = inJdMentionCount;
        this.priorityScore = priorityScore;
        this.priorityTier = priorityTier;
        this.learnOrder = learnOrder;
    }

    public String getSkillName() {
        return skillName;
    }

    public int getCrossReportCount() {
        return crossReportCount;
    }

    public int getInJdMentionCount() {
        return inJdMentionCount;
    }

    public int getPriorityScore() {
        return priorityScore;
    }

    public PriorityTier getPriorityTier() {
        return priorityTier;
    }

    public LearnOrder getLearnOrder() {
        return learnOrder;
    }
}
