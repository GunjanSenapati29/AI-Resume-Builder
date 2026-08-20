package com.skillgapai.career;

import com.skillgapai.dto.RoleFitView;
import com.skillgapai.dto.RoleRecommendationsView;
import com.skillgapai.entity.GapReport;
import com.skillgapai.repository.GapReportRepository;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

/**
 * Phase 20: scores the user's aggregate skill profile against a fixed
 * catalog of role categories - same static-lookup-table architecture as
 * Phase 17's OFFICIAL_DOCS/PROJECT_IDEAS and Phase 19's
 * CURATED_QUESTIONS, no DB table, no ML.
 *
 * Unlike Phase 17-19 (which only ever look at the single most recent
 * GapReport), this reads across EVERY report the user has ever run:
 * a skill counts toward a role's score if it was ever MATCHED on any
 * report, not just the latest one. That's deliberate - a role fit is a
 * question about the user's overall demonstrated skill set, not about
 * one specific JD comparison.
 *
 * Computed live on every call, nothing persisted - same reasoning as
 * Phase 17-19: this is a derived view over existing GapReport data, not
 * a scored value that needs to stay stable across re-reads.
 */
@Service
public class CareerRoleService {

    private static final Map<String, List<String>> CAREER_ROLES = new LinkedHashMap<>();

    static {
        CAREER_ROLES.put("Backend Developer (Java)", List.of("Java", "Spring Boot", "MySQL", "REST APIs"));
        CAREER_ROLES.put("Full Stack Developer", List.of("Java", "Spring Boot", "MySQL", "REST APIs", "JavaScript", "React"));
        CAREER_ROLES.put("Frontend Developer", List.of("JavaScript", "React", "Git"));
        CAREER_ROLES.put("DevOps-leaning Engineer", List.of("Docker", "Git", "MySQL"));
        CAREER_ROLES.put("QA / SDET", List.of("Java", "JUnit", "Git"));
    }

    private final GapReportRepository gapReportRepository;
    private final ObjectMapper objectMapper;

    public CareerRoleService(GapReportRepository gapReportRepository, ObjectMapper objectMapper) {
        this.gapReportRepository = gapReportRepository;
        this.objectMapper = objectMapper;
    }

    public Optional<RoleRecommendationsView> buildForAllReports(String userEmail) {
        List<GapReport> reports = gapReportRepository.findByResume_User_EmailOrderByCreatedAtDesc(userEmail);
        if (reports.isEmpty()) {
            return Optional.empty();
        }

        Set<String> everMatchedLower = new TreeSet<>();
        for (GapReport report : reports) {
            everMatchedLower.addAll(skillNames(report.getMatchedSkillsJson()).stream()
                    .map(String::toLowerCase)
                    .toList());
        }

        List<RoleFitView> roles = CAREER_ROLES.entrySet().stream()
                .map(entry -> scoreRole(entry.getKey(), entry.getValue(), everMatchedLower))
                .sorted(Comparator.comparingInt(RoleFitView::score).reversed())
                .toList();

        return Optional.of(new RoleRecommendationsView(roles));
    }

    private RoleFitView scoreRole(String roleName, List<String> requiredSkills, Set<String> everMatchedLower) {
        List<String> matched = requiredSkills.stream()
                .filter(skill -> everMatchedLower.contains(skill.toLowerCase()))
                .toList();
        List<String> missing = requiredSkills.stream()
                .filter(skill -> !everMatchedLower.contains(skill.toLowerCase()))
                .toList();

        int score = Math.round(matched.size() * 100f / requiredSkills.size());

        return new RoleFitView(roleName, score, matched, missing);
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
