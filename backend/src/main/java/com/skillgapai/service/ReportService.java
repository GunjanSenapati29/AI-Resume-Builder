package com.skillgapai.service;

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
 * Resume/User rows it depends on) and builds the GapReportView the API
 * returns, either right after creating a report or later via
 * GET /api/reports/{id}.
 *
 * There's no resume upload (Phase 5) or login (Phase 6e) yet, so every
 * report is attached to one fixed "guest" User row - found by email if
 * it already exists, created once otherwise - and a fresh Resume row is
 * inserted per match. Once real auth/upload exist, the guest lookup
 * goes away and the resume/user come from the logged-in request instead;
 * nothing else in this class has to change.
 */
@Service
public class ReportService {

    private static final String GUEST_EMAIL = "guest@skillgap.local";

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
    public GapReportView createReport(String resumeText, String jdText, List<RequiredSkill> requiredSkills) {
        User guest = userRepository.findByEmail(GUEST_EMAIL)
                .orElseGet(() -> userRepository.save(new User("Guest", GUEST_EMAIL, "no-auth-yet")));

        Resume resume = resumeRepository.save(new Resume(guest, resumeText, LocalDateTime.now()));

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

    @Transactional(readOnly = true)
    public Optional<GapReportView> getReport(Long id) {
        return gapReportRepository.findById(id).map(this::toView);
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
