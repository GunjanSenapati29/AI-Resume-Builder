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
 *
 * Phase 18: for the fixed list of skills in PROJECT_IDEAS, step 2 of the
 * 4-step template is replaced with a concrete, curated project idea
 * instead of the generic "practice with a small project" phrasing. Any
 * skill not in that list keeps the original generic step 2 unchanged.
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

    // A concrete project idea for step 2, for the fixed list of skills
    // below. Any skill not listed here keeps the generic step 2 text.
    private static final Map<String, String> PROJECT_IDEAS = Map.ofEntries(
            Map.entry("java", "Build a command-line library management system using core Java, collections, and file I/O to persist data."),
            Map.entry("spring boot", "Build a small REST API (e.g. a personal expense tracker) with Spring Boot, exposing CRUD endpoints backed by a database."),
            Map.entry("javascript", "Build an interactive to-do list or quiz app using vanilla JavaScript, DOM manipulation, and local state."),
            Map.entry("python", "Build a command-line tool that automates a repetitive task (e.g. renaming files, parsing a CSV) using Python's standard library."),
            Map.entry("react", "Build a small multi-page app (e.g. a movie search app using a public API) using React components, state, and routing."),
            Map.entry("mysql", "Design a relational database schema for a real-world scenario (e.g. a bookstore) and write queries covering joins, aggregates, and subqueries."),
            Map.entry("git", "Practice a full branching workflow on one of your existing projects - create feature branches, open pull requests, and resolve a merge conflict."),
            Map.entry("docker", "Containerize one of your existing projects with a Dockerfile, and use docker-compose to run it alongside a database."),
            Map.entry("aws", "Deploy a small project to AWS (e.g. host a static site on S3, or run a simple app on an EC2 instance) and document the steps."),
            Map.entry("rest apis", "Design and build a simple REST API for a small domain (e.g. a notes app), following proper HTTP methods and status codes."),
            Map.entry("junit", "Add unit tests to one of your existing projects using JUnit, covering both normal and edge-case inputs."));

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
                projectIdeaStep(skillName),
                "Add " + skillName + " to your resume with real evidence (a project, a bullet point)",
                "Re-run SkillGap AI to confirm it's now detected");
    }

    private String projectIdeaStep(String skillName) {
        String projectIdea = PROJECT_IDEAS.get(skillName.toLowerCase());
        return projectIdea != null ? projectIdea : "Practice with a small project using " + skillName;
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
