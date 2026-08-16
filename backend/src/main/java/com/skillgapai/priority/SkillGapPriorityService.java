package com.skillgapai.priority;

import com.skillgapai.entity.GapReport;
import com.skillgapai.model.LearnOrder;
import com.skillgapai.model.MatchResult;
import com.skillgapai.model.PriorityTier;
import com.skillgapai.model.SkillGapPriorityResult;
import com.skillgapai.repository.GapReportRepository;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Phase 15: for each skill SkillMatchingService already found missing on
 * the current report, scores how urgently it's worth closing from two
 * signals and combines them into a 0-4 priority score:
 *
 *  - Cross-report recurrence: how many of this user's OTHER past
 *    reports also required this skill. Reuses the matched/missing JSON
 *    already stored on every GapReport row (a skill was "required" on a
 *    past report if it shows up in either list there) instead of adding
 *    a new tracking table - the data already exists.
 *  - In-JD emphasis: how many times the skill term is mentioned in the
 *    current job description text.
 *
 * 0-4 score -> PriorityTier (3-4 CRITICAL, 1-2 IMPORTANT, 0 OPTIONAL) ->
 * LearnOrder (1:1 from tier). Runs entirely off data SkillMatchingService
 * already produced and the JD text already submitted - doesn't alter
 * either.
 */
@Service
public class SkillGapPriorityService {

    private final GapReportRepository gapReportRepository;
    private final ObjectMapper objectMapper;

    public SkillGapPriorityService(GapReportRepository gapReportRepository, ObjectMapper objectMapper) {
        this.gapReportRepository = gapReportRepository;
        this.objectMapper = objectMapper;
    }

    public List<SkillGapPriorityResult> analyze(String userEmail, String jdText, List<MatchResult> missingSkills) {
        if (missingSkills.isEmpty()) {
            return List.of();
        }

        // Every skill required on a past report ends up in that report's
        // matched OR missing list (SkillMatchingService puts every
        // required skill in exactly one of the two) - so their union is
        // "what that report's JD required", without needing to have
        // stored the raw required-skills list separately.
        List<Set<String>> pastRequiredSkillSets = gapReportRepository.findByResume_User_EmailOrderByCreatedAtDesc(userEmail)
                .stream()
                .map(this::requiredSkillNames)
                .toList();

        String normalizedJd = (jdText == null ? "" : jdText).toLowerCase();

        List<SkillGapPriorityResult> results = new ArrayList<>();
        for (MatchResult missing : missingSkills) {
            String skillName = missing.getSkillName();
            String key = skillName.toLowerCase();

            long crossReportCount = pastRequiredSkillSets.stream().filter(set -> set.contains(key)).count();
            int crossReportPoints = crossReportCount == 0 ? 0 : (crossReportCount == 1 ? 1 : 2);

            int inJdMentionCount = countOccurrences(normalizedJd, key);
            int inJdPoints = inJdMentionCount >= 3 ? 2 : (inJdMentionCount == 2 ? 1 : 0);

            int score = crossReportPoints + inJdPoints;
            PriorityTier tier = score >= 3 ? PriorityTier.CRITICAL : (score >= 1 ? PriorityTier.IMPORTANT : PriorityTier.OPTIONAL);
            LearnOrder learnOrder = switch (tier) {
                case CRITICAL -> LearnOrder.LEARN_FIRST;
                case IMPORTANT -> LearnOrder.LEARN_NEXT;
                case OPTIONAL -> LearnOrder.LEARN_LATER;
            };

            results.add(new SkillGapPriorityResult(skillName, (int) crossReportCount, inJdMentionCount, score, tier, learnOrder));
        }
        return results;
    }

    private Set<String> requiredSkillNames(GapReport report) {
        Set<String> names = new HashSet<>();
        addSkillNames(names, report.getMatchedSkillsJson());
        addSkillNames(names, report.getMissingSkillsJson());
        return names;
    }

    private void addSkillNames(Set<String> names, String json) {
        JsonNode node = objectMapper.readTree(json);
        for (JsonNode item : node) {
            names.add(item.path("skillName").asText("").toLowerCase());
        }
    }

    private int countOccurrences(String normalizedText, String term) {
        if (term.isBlank()) {
            return 0;
        }
        Pattern pattern = Pattern.compile("\\b" + Pattern.quote(term) + "\\b");
        Matcher matcher = pattern.matcher(normalizedText);
        int count = 0;
        while (matcher.find()) {
            count++;
        }
        return count;
    }
}
