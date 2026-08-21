package com.skillgapai.resumebuilder;

import com.skillgapai.dto.ResumeVersionComparisonView;
import com.skillgapai.dto.ResumeVersionMatchView;
import com.skillgapai.matching.SkillMatchingService;
import com.skillgapai.model.GapAnalysisResult;
import com.skillgapai.model.RequiredSkill;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Phase 23: runs the existing, unchanged SkillMatchingService once per
 * resume version (flattened to plain text by ResumeVersionService)
 * against the same jdText/requiredSkills, and returns both results side
 * by side. Computed live, never persisted - this is an ad-hoc "what if"
 * comparison, not a saved GapReport/History entry.
 *
 * Same ownership convention as everywhere else: if ANY requested id
 * doesn't resolve to one of the logged-in user's own resume versions,
 * the whole request comes back empty (404 at the controller) rather
 * than revealing which id failed.
 */
@Service
public class ResumeVersionComparisonService {

    private final ResumeVersionService resumeVersionService;
    private final SkillMatchingService matchingService;

    public ResumeVersionComparisonService(ResumeVersionService resumeVersionService, SkillMatchingService matchingService) {
        this.resumeVersionService = resumeVersionService;
        this.matchingService = matchingService;
    }

    @Transactional(readOnly = true)
    public Optional<ResumeVersionComparisonView> compare(List<Long> ids, String userEmail, String jdText,
                                                           List<RequiredSkill> requiredSkills) {
        List<ResumeVersionMatchView> results = new ArrayList<>();
        for (Long id : ids) {
            Optional<FlattenedResumeVersion> flattened = resumeVersionService.flatten(id, userEmail);
            if (flattened.isEmpty()) {
                return Optional.empty();
            }

            GapAnalysisResult result = matchingService.analyze(flattened.get().text(), requiredSkills);
            results.add(new ResumeVersionMatchView(
                    id,
                    flattened.get().title(),
                    result.getMatched(),
                    result.getMissing(),
                    result.getUnderemphasized(),
                    result.getMatchPercentage()));
        }

        results.sort(Comparator.comparingDouble(ResumeVersionMatchView::matchPercentage).reversed());
        return Optional.of(new ResumeVersionComparisonView(jdText, results));
    }
}
