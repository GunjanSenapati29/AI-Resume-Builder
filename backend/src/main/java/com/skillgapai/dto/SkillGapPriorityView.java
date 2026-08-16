package com.skillgapai.dto;

import com.skillgapai.model.LearnOrder;
import com.skillgapai.model.PriorityTier;

/**
 * JSON shape of one missing skill's priority classification, nested
 * inside GapReportView.skillGapPriorities - see SkillGapPriorityService.
 */
public record SkillGapPriorityView(String skillName, int crossReportCount, int inJdMentionCount,
                                    int priorityScore, PriorityTier priorityTier, LearnOrder learnOrder) {
}
