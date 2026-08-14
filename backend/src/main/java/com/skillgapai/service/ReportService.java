package com.skillgapai.service;

import com.skillgapai.dto.GapReportSummary;
import com.skillgapai.dto.GapReportView;
import com.skillgapai.entity.GapReport;
import com.skillgapai.entity.Resume;
import com.skillgapai.entity.User;
import com.skillgapai.matching.SkillMatchingService;
import com.skillgapai.model.GapAnalysisResult;
import com.skillgapai.model.RequiredSkill;
import com.skillgapai.repository.GapReportRepository;
import com.skillgapai.repository.ResumeRepository;
import com.skillgapai.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Phase 4: turns one match run into a persisted GapReport row (plus the
 * Resume row it depends on) and builds the GapReportView the API
 * returns, either right after creating a report or later via
 * GET /api/reports/{id}.
 *
 * Phase 6e: every method takes the requesting user's email (read from
 * the JWT by the controller, via SecurityContextHolder) instead of the
 * old fixed "guest" placeholder - reports are now attached to, and
 * scoped to, whoever is actually logged in.
 */
@Service
public class ReportService {

    private static final int JD_SNIPPET_LENGTH = 80;

    private final SkillMatchingService matchingService;
    private final UserRepository userRepository;
    private final ResumeRepository resumeRepository;
    private final GapReportRepository gapReportRepository;
    private final ObjectMapper objectMapper;

    public ReportService(SkillMatchingService matchingService,
                          UserRepository userRepository,
                          ResumeRepository resumeRepository,
                          GapReportRepository gapReportRepository,
                          ObjectMapper objectMapper) {
        this.matchingService = matchingService;
        this.userRepository = userRepository;
        this.resumeRepository = resumeRepository;
        this.gapReportRepository = gapReportRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public GapReportView createReport(String userEmail, String resumeText, String jdText, List<RequiredSkill> requiredSkills) {
        // Authenticated by JwtAuthenticationFilter before this is ever
        // called, so the user is guaranteed to exist.
        User user = userRepository.findByEmail(userEmail).orElseThrow();

        Resume resume = resumeRepository.save(new Resume(user, resumeText, LocalDateTime.now()));

        GapAnalysisResult result = matchingService.analyze(resumeText, requiredSkills);

        GapReport report = new GapReport(
                resume,
                jdText,
                toJson(result.getMatched()),
                toJson(result.getMissing()),
                toJson(result.getUnderemphasized()),
                result.getMatchPercentage(),
                LocalDateTime.now());
        report = gapReportRepository.save(report);

        return new GapReportView(
                report.getId(),
                resume.getId(),
                jdText,
                objectMapper.valueToTree(result.getMatched()),
                objectMapper.valueToTree(result.getMissing()),
                objectMapper.valueToTree(result.getUnderemphasized()),
                result.getMatchPercentage(),
                report.getCreatedAt());
    }

    /**
     * Phase 6e: scoped to the requesting user - a report that exists but
     * belongs to someone else is reported as "not found" rather than
     * "forbidden", so this endpoint never confirms or denies that a
     * given report id belongs to another account.
     */
    @Transactional(readOnly = true)
    public Optional<GapReportView> getReport(Long id, String userEmail) {
        return gapReportRepository.findById(id)
                .filter(report -> report.getResume().getUser().getEmail().equals(userEmail))
                .map(this::toView);
    }

    /**
     * Phase 6d: past reports for the History screen, most recent first.
     * Phase 6e: scoped to the logged-in user's email instead of the old
     * fixed guest placeholder.
     */
    @Transactional(readOnly = true)
    public List<GapReportSummary> listReportHistory(String userEmail) {
        return gapReportRepository.findByResume_User_EmailOrderByCreatedAtDesc(userEmail).stream()
                .map(report -> new GapReportSummary(
                        report.getId(),
                        snippet(report.getJdText()),
                        report.getMatchPercentage(),
                        report.getCreatedAt()))
                .toList();
    }

    private String snippet(String text) {
        String trimmed = text.strip();
        if (trimmed.length() <= JD_SNIPPET_LENGTH) {
            return trimmed;
        }
        return trimmed.substring(0, JD_SNIPPET_LENGTH).stripTrailing() + "...";
    }

    private GapReportView toView(GapReport report) {
        return new GapReportView(
                report.getId(),
                report.getResume().getId(),
                report.getJdText(),
                readJson(report.getMatchedSkillsJson()),
                readJson(report.getMissingSkillsJson()),
                readJson(report.getUnderemphasizedSkillsJson()),
                report.getMatchPercentage(),
                report.getCreatedAt());
    }

    private String toJson(Object value) {
        return objectMapper.writeValueAsString(value);
    }

    private JsonNode readJson(String json) {
        return objectMapper.readTree(json);
    }
}
