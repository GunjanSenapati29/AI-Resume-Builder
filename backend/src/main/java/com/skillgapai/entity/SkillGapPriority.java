package com.skillgapai.entity;

import com.skillgapai.model.LearnOrder;
import com.skillgapai.model.PriorityTier;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Phase 15: maps to the "skill_gap_priorities" table. One row per
 * missing skill on a GapReport, recording the two signals that fed its
 * priority score (cross-report recurrence, in-JD mention count) and the
 * resulting tier/learn-order - see SkillGapPriorityService.
 */
@Entity
@Table(name = "skill_gap_priorities")
public class SkillGapPriority {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "skill_gap_priority_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "report_id", nullable = false)
    private GapReport report;

    @Column(name = "skill_name", nullable = false)
    private String skillName;

    @Column(name = "cross_report_count", nullable = false)
    private int crossReportCount;

    @Column(name = "in_jd_mention_count", nullable = false)
    private int inJdMentionCount;

    @Column(name = "priority_score", nullable = false)
    private int priorityScore;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority_tier", nullable = false, length = 15)
    private PriorityTier priorityTier;

    @Enumerated(EnumType.STRING)
    @Column(name = "learn_order", nullable = false, length = 15)
    private LearnOrder learnOrder;

    protected SkillGapPriority() {
        // required by JPA
    }

    public SkillGapPriority(GapReport report, String skillName, int crossReportCount, int inJdMentionCount,
                             int priorityScore, PriorityTier priorityTier, LearnOrder learnOrder) {
        this.report = report;
        this.skillName = skillName;
        this.crossReportCount = crossReportCount;
        this.inJdMentionCount = inJdMentionCount;
        this.priorityScore = priorityScore;
        this.priorityTier = priorityTier;
        this.learnOrder = learnOrder;
    }

    public Long getId() {
        return id;
    }

    public GapReport getReport() {
        return report;
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
