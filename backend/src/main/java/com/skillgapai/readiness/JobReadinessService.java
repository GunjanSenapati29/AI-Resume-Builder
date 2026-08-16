package com.skillgapai.readiness;

import com.skillgapai.model.EvidenceLevel;
import com.skillgapai.model.JobReadinessLabel;
import com.skillgapai.model.JobReadinessResult;
import com.skillgapai.model.PriorityTier;
import com.skillgapai.model.SkillEvidenceResult;
import com.skillgapai.model.SkillGapPriorityResult;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Phase 16: combines four numbers already produced by earlier phases into
 * one composite, explainable Job Readiness Score - doesn't recompute or
 * alter any of them:
 *
 *  - Skill Match Score (40%): SkillMatchingService's overall match
 *    percentage, unchanged.
 *  - ATS Compatibility Score (20%): AtsAnalyzerService's 0-100 score,
 *    unchanged.
 *  - Evidence Strength Score (20%): the average, over every MATCHED
 *    skill, of its Phase 14 evidence level mapped to a 0-100 value
 *    (STRONG=100, MODERATE=67, WEAK=33, NO_EVIDENCE=0). A report with no
 *    matched skills has no evidence to show for itself, so this scores 0
 *    rather than being skipped.
 *  - Gap Severity Score (20%): starts at 100 and subtracts per MISSING
 *    skill by its Phase 15 priority tier (CRITICAL -30, IMPORTANT -15,
 *    OPTIONAL -5), floored at 0.
 *
 * The final score is a straight weighted sum, rounded once at the end -
 * the weights are fixed and intentionally not configurable, so the
 * formula stays as transparent as every other rule-based part of this
 * project.
 */
@Service
public class JobReadinessService {

    private static final double SKILL_MATCH_WEIGHT = 0.40;
    private static final double ATS_WEIGHT = 0.20;
    private static final double EVIDENCE_WEIGHT = 0.20;
    private static final double GAP_SEVERITY_WEIGHT = 0.20;

    private static final Map<EvidenceLevel, Integer> EVIDENCE_LEVEL_VALUES = Map.of(
            EvidenceLevel.STRONG, 100,
            EvidenceLevel.MODERATE, 67,
            EvidenceLevel.WEAK, 33,
            EvidenceLevel.NO_EVIDENCE, 0);

    private static final Map<PriorityTier, Integer> TIER_DEDUCTIONS = Map.of(
            PriorityTier.CRITICAL, 30,
            PriorityTier.IMPORTANT, 15,
            PriorityTier.OPTIONAL, 5);

    public JobReadinessResult compute(double matchPercentage, int atsScore,
                                       List<SkillEvidenceResult> evidenceResults,
                                       List<SkillGapPriorityResult> priorityResults) {
        double evidenceStrengthScore = evidenceStrengthScore(evidenceResults);
        int gapSeverityScore = gapSeverityScore(priorityResults);

        double weightedSum = matchPercentage * SKILL_MATCH_WEIGHT
                + atsScore * ATS_WEIGHT
                + evidenceStrengthScore * EVIDENCE_WEIGHT
                + gapSeverityScore * GAP_SEVERITY_WEIGHT;

        int overallScore = (int) Math.round(weightedSum);

        return new JobReadinessResult(
                overallScore,
                labelFor(overallScore),
                (int) Math.round(matchPercentage),
                atsScore,
                (int) Math.round(evidenceStrengthScore),
                gapSeverityScore);
    }

    private double evidenceStrengthScore(List<SkillEvidenceResult> evidenceResults) {
        if (evidenceResults.isEmpty()) {
            return 0;
        }
        int total = evidenceResults.stream()
                .mapToInt(evidence -> EVIDENCE_LEVEL_VALUES.get(evidence.getEvidenceLevel()))
                .sum();
        return total / (double) evidenceResults.size();
    }

    private int gapSeverityScore(List<SkillGapPriorityResult> priorityResults) {
        int totalDeductions = priorityResults.stream()
                .mapToInt(priority -> TIER_DEDUCTIONS.get(priority.getPriorityTier()))
                .sum();
        return Math.max(0, 100 - totalDeductions);
    }

    private JobReadinessLabel labelFor(int overallScore) {
        if (overallScore >= 85) {
            return JobReadinessLabel.EXCELLENT;
        }
        if (overallScore >= 70) {
            return JobReadinessLabel.STRONG;
        }
        if (overallScore >= 50) {
            return JobReadinessLabel.NEEDS_WORK;
        }
        return JobReadinessLabel.NOT_READY;
    }
}
