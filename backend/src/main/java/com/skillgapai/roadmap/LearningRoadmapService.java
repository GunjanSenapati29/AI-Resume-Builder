package com.skillgapai.roadmap;

import com.skillgapai.dto.LearningRoadmapView;
import com.skillgapai.dto.SkillRoadmapItemView;
import com.skillgapai.entity.GapReport;
import com.skillgapai.entity.SkillGapPriority;
import com.skillgapai.model.PriorityTier;
import com.skillgapai.repository.GapReportRepository;
import com.skillgapai.repository.SkillGapPriorityRepository;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Phase 17: turns the missing/underemphasized skills from the user's
 * MOST RECENT GapReport into a Learning Roadmap - reads Phase 15's
 * SkillGapPriority data for the missing skills' tier, but doesn't alter
 * any Phase 13-16 logic. Computed live on every call (not persisted):
 * it's a static template + a fixed lookup table, not a scored value
 * that needs to stay stable across re-reads the way the Job Readiness
 * Score does.
 *
 * Content is deliberately generic - see buildSteps and OFFICIAL_DOCS -
 * this never invents a course name, tutorial title, or URL. The only
 * per-skill variation is the skill name itself and, for the fixed list
 * of skills in OFFICIAL_DOCS, a link to that skill's real, stable,
 * official documentation homepage.
 */
@Service
public class LearningRoadmapService {

    // Homepage only - nothing deeper, nothing invented. Any skill not
    // listed here gets the 4-step template with no link.
    private static final Map<String, String> OFFICIAL_DOCS = Map.ofEntries(
            Map.entry("java", "https://docs.oracle.com/javase"),
            Map.entry("javascript", "https://developer.mozilla.org"),
            Map.entry("python", "https://docs.python.org"),
            Map.entry("react", "https://react.dev"),
            Map.entry("spring boot", "https://spring.io/projects/spring-boot"),
            Map.entry("mysql", "https://dev.mysql.com/doc"),
            Map.entry("git", "https://git-scm.com/doc"),
            Map.entry("docker", "https://docs.docker.com"),
            Map.entry("aws", "https://docs.aws.amazon.com"));

    private final GapReportRepository gapReportRepository;
    private final SkillGapPriorityRepository skillGapPriorityRepository;
    private final ObjectMapper objectMapper;

    public LearningRoadmapService(GapReportRepository gapReportRepository,
                                   SkillGapPriorityRepository skillGapPriorityRepository,
                                   ObjectMapper objectMapper) {
        this.gapReportRepository = gapReportRepository;
        this.skillGapPriorityRepository = skillGapPriorityRepository;
        this.objectMapper = objectMapper;
    }

    public Optional<LearningRoadmapView> buildForLatestReport(String userEmail) {
        Optional<GapReport> latestReport = gapReportRepository.findFirstByResume_User_EmailOrderByCreatedAtDesc(userEmail);
        if (latestReport.isEmpty()) {
            return Optional.empty();
        }
        GapReport report = latestReport.get();

        // Every CURRENT missing skill gets a SkillGapPriority row (Phase
        // 15 runs over every missing skill, always) - this map can only
        // come up short for a report saved before Phase 15 existed, same
        // old-report-compatibility gap Phase 15/16 already have.
        Map<String, PriorityTier> tierBySkill = skillGapPriorityRepository.findByReport_IdOrderById(report.getId()).stream()
                .collect(Collectors.toMap(
                        priority -> priority.getSkillName().toLowerCase(),
                        SkillGapPriority::getPriorityTier));

        List<SkillRoadmapItemView> missingItems = skillNames(report.getMissingSkillsJson()).stream()
                .map(skillName -> toItem(skillName, tierBySkill.get(skillName.toLowerCase())))
                .sorted(Comparator.comparingInt(item -> item.priorityTier() == null ? Integer.MAX_VALUE : item.priorityTier().ordinal()))
                .toList();

        List<SkillRoadmapItemView> underemphasizedItems = skillNames(report.getUnderemphasizedSkillsJson()).stream()
                .map(skillName -> toItem(skillName, null))
                .toList();

        return Optional.of(new LearningRoadmapView(report.getId(), missingItems, underemphasizedItems));
    }

    private SkillRoadmapItemView toItem(String skillName, PriorityTier priorityTier) {
        return new SkillRoadmapItemView(skillName, priorityTier, buildSteps(skillName), officialDocUrl(skillName));
    }

    private List<String> buildSteps(String skillName) {
        return List.of(
                "Learn the fundamentals of " + skillName,
                "Practice with a small project using " + skillName,
                "Add " + skillName + " to your resume with real evidence (a project, a bullet point)",
                "Re-run SkillGap AI to confirm it's now detected");
    }

    private String officialDocUrl(String skillName) {
        return OFFICIAL_DOCS.get(skillName.toLowerCase());
    }

    private List<String> skillNames(String json) {
        JsonNode node = objectMapper.readTree(json);
        List<String> names = new ArrayList<>();
        for (JsonNode item : node) {
            names.add(item.path("skillName").asText(""));
        }
        return names;
    }
}
