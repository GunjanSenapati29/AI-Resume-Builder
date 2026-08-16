package com.skillgapai.service;

import com.skillgapai.ats.AtsAnalyzerService;
import com.skillgapai.dto.AtsIssueView;
import com.skillgapai.dto.GapReportSummary;
import com.skillgapai.dto.GapReportView;
import com.skillgapai.dto.SkillEvidenceView;
import com.skillgapai.dto.SkillGapPriorityView;
import com.skillgapai.entity.AtsIssue;
import com.skillgapai.entity.GapReport;
import com.skillgapai.entity.Resume;
import com.skillgapai.entity.SkillEvidence;
import com.skillgapai.entity.SkillGapPriority;
import com.skillgapai.entity.User;
import com.skillgapai.evidence.SkillEvidenceService;
import com.skillgapai.matching.SkillMatchingService;
import com.skillgapai.model.AtsAnalysisResult;
import com.skillgapai.model.AtsCheckResult;
import com.skillgapai.model.GapAnalysisResult;
import com.skillgapai.model.RequiredSkill;
import com.skillgapai.model.SkillEvidenceResult;
import com.skillgapai.model.SkillGapPriorityResult;
import com.skillgapai.priority.SkillGapPriorityService;
import com.skillgapai.repository.AtsIssueRepository;
import com.skillgapai.repository.GapReportRepository;
import com.skillgapai.repository.ResumeRepository;
import com.skillgapai.repository.SkillEvidenceRepository;
import com.skillgapai.repository.SkillGapPriorityRepository;
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
 *
 * Phase 13: createReport also runs AtsAnalyzerService against the same
 * resumeText and persists its per-check results as AtsIssue rows -
 * additive alongside the existing skill-matching flow, not a
 * replacement for any part of it.
 *
 * Phase 14: createReport also runs SkillEvidenceService against the
 * matched skills from the same analysis run and persists the result as
 * SkillEvidence rows - again additive, doesn't touch the matching or
 * ATS results above it.
 *
 * Phase 15: createReport also runs SkillGapPriorityService against the
 * missing skills from the same analysis run (plus this user's past
 * reports and the current JD text) and persists the result as
 * SkillGapPriority rows - again additive, doesn't touch anything above
 * it. Must run before the current report is saved, since cross-report
 * recurrence is counted against the user's OTHER, already-saved reports.
 */
@Service
public class ReportService {

    private static final int JD_SNIPPET_LENGTH = 80;

    private final SkillMatchingService matchingService;
    private final AtsAnalyzerService atsAnalyzerService;
    private final SkillEvidenceService skillEvidenceService;
    private final SkillGapPriorityService skillGapPriorityService;
    private final UserRepository userRepository;
    private final ResumeRepository resumeRepository;
    private final GapReportRepository gapReportRepository;
    private final AtsIssueRepository atsIssueRepository;
    private final SkillEvidenceRepository skillEvidenceRepository;
    private final SkillGapPriorityRepository skillGapPriorityRepository;
    private final ObjectMapper objectMapper;

    public ReportService(SkillMatchingService matchingService,
                          AtsAnalyzerService atsAnalyzerService,
                          SkillEvidenceService skillEvidenceService,
                          SkillGapPriorityService skillGapPriorityService,
                          UserRepository userRepository,
                          ResumeRepository resumeRepository,
                          GapReportRepository gapReportRepository,
                          AtsIssueRepository atsIssueRepository,
                          SkillEvidenceRepository skillEvidenceRepository,
                          SkillGapPriorityRepository skillGapPriorityRepository,
                          ObjectMapper objectMapper) {
        this.matchingService = matchingService;
        this.atsAnalyzerService = atsAnalyzerService;
        this.skillEvidenceService = skillEvidenceService;
        this.skillGapPriorityService = skillGapPriorityService;
        this.userRepository = userRepository;
        this.resumeRepository = resumeRepository;
        this.gapReportRepository = gapReportRepository;
        this.atsIssueRepository = atsIssueRepository;
        this.skillEvidenceRepository = skillEvidenceRepository;
        this.skillGapPriorityRepository = skillGapPriorityRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public GapReportView createReport(String userEmail, String resumeText, String jdText, List<RequiredSkill> requiredSkills) {
        // Authenticated by JwtAuthenticationFilter before this is ever
        // called, so the user is guaranteed to exist.
        User user = userRepository.findByEmail(userEmail).orElseThrow();

        Resume resume = resumeRepository.save(new Resume(user, resumeText, LocalDateTime.now()));

        GapAnalysisResult result = matchingService.analyze(resumeText, requiredSkills);
        // Phase 13: runs alongside skill matching, off the same
        // resumeText, in the same request/transaction - doesn't touch
        // the matching result above at all.
        AtsAnalysisResult atsResult = atsAnalyzerService.analyze(resumeText);
        // Phase 14: also off the same resumeText, plus the matched-skill
        // list the analysis above just produced - runs after it, reads
        // it, doesn't change it.
        List<SkillEvidenceResult> evidenceResults = skillEvidenceService.analyze(resumeText, result.getMatched());
        // Phase 15: off the missing-skill list above, this JD's text, and
        // this user's past reports - must run before this report is
        // saved below, since "past reports" means every OTHER report
        // this query would find.
        List<SkillGapPriorityResult> priorityResults = skillGapPriorityService.analyze(userEmail, jdText, result.getMissing());

        GapReport report = gapReportRepository.save(new GapReport(
                resume,
                jdText,
                toJson(result.getMatched()),
                toJson(result.getMissing()),
                toJson(result.getUnderemphasized()),
                result.getMatchPercentage(),
                atsResult.getScore(),
                LocalDateTime.now()));

        List<AtsIssue> issues = atsResult.getChecks().stream()
                .map(check -> new AtsIssue(report, check.getTitle(), check.getDescription(),
                        check.getFixSuggestion(), check.getSeverity()))
                .toList();
        atsIssueRepository.saveAll(issues);

        List<SkillEvidence> evidenceRows = evidenceResults.stream()
                .map(evidence -> new SkillEvidence(report, evidence.getSkillName(),
                        evidence.isInSkillsSection(), evidence.isInProjectsSection(),
                        evidence.isInExperienceSection(), evidence.getEvidenceLevel()))
                .toList();
        skillEvidenceRepository.saveAll(evidenceRows);

        List<SkillGapPriority> priorityRows = priorityResults.stream()
                .map(priority -> new SkillGapPriority(report, priority.getSkillName(), priority.getCrossReportCount(),
                        priority.getInJdMentionCount(), priority.getPriorityScore(), priority.getPriorityTier(),
                        priority.getLearnOrder()))
                .toList();
        skillGapPriorityRepository.saveAll(priorityRows);

        return new GapReportView(
                report.getId(),
                resume.getId(),
                jdText,
                objectMapper.valueToTree(result.getMatched()),
                objectMapper.valueToTree(result.getMissing()),
                objectMapper.valueToTree(result.getUnderemphasized()),
                result.getMatchPercentage(),
                report.getCreatedAt(),
                atsResult.getScore(),
                atsResult.getChecks().stream().map(this::toAtsIssueView).toList(),
                evidenceResults.stream().map(this::toSkillEvidenceView).toList(),
                priorityResults.stream().map(this::toSkillGapPriorityView).toList());
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
        List<AtsIssueView> atsIssues = atsIssueRepository.findByReport_IdOrderById(report.getId()).stream()
                .map(this::toAtsIssueView)
                .toList();
        List<SkillEvidenceView> skillEvidence = skillEvidenceRepository.findByReport_IdOrderById(report.getId()).stream()
                .map(this::toSkillEvidenceView)
                .toList();
        List<SkillGapPriorityView> skillGapPriorities = skillGapPriorityRepository.findByReport_IdOrderById(report.getId()).stream()
                .map(this::toSkillGapPriorityView)
                .toList();

        return new GapReportView(
                report.getId(),
                report.getResume().getId(),
                report.getJdText(),
                readJson(report.getMatchedSkillsJson()),
                readJson(report.getMissingSkillsJson()),
                readJson(report.getUnderemphasizedSkillsJson()),
                report.getMatchPercentage(),
                report.getCreatedAt(),
                report.getAtsScore(),
                atsIssues,
                skillEvidence,
                skillGapPriorities);
    }

    private AtsIssueView toAtsIssueView(AtsCheckResult check) {
        return new AtsIssueView(check.getTitle(), check.getDescription(), check.getFixSuggestion(), check.getSeverity());
    }

    private AtsIssueView toAtsIssueView(AtsIssue issue) {
        return new AtsIssueView(issue.getTitle(), issue.getDescription(), issue.getFixSuggestion(), issue.getSeverity());
    }

    private SkillEvidenceView toSkillEvidenceView(SkillEvidenceResult evidence) {
        return new SkillEvidenceView(evidence.getSkillName(), evidence.isInSkillsSection(),
                evidence.isInProjectsSection(), evidence.isInExperienceSection(), evidence.getEvidenceLevel());
    }

    private SkillEvidenceView toSkillEvidenceView(SkillEvidence evidence) {
        return new SkillEvidenceView(evidence.getSkillName(), evidence.isInSkillsSection(),
                evidence.isInProjectsSection(), evidence.isInExperienceSection(), evidence.getEvidenceLevel());
    }

    private SkillGapPriorityView toSkillGapPriorityView(SkillGapPriorityResult priority) {
        return new SkillGapPriorityView(priority.getSkillName(), priority.getCrossReportCount(),
                priority.getInJdMentionCount(), priority.getPriorityScore(), priority.getPriorityTier(),
                priority.getLearnOrder());
    }

    private SkillGapPriorityView toSkillGapPriorityView(SkillGapPriority priority) {
        return new SkillGapPriorityView(priority.getSkillName(), priority.getCrossReportCount(),
                priority.getInJdMentionCount(), priority.getPriorityScore(), priority.getPriorityTier(),
                priority.getLearnOrder());
    }

    private String toJson(Object value) {
        return objectMapper.writeValueAsString(value);
    }

    private JsonNode readJson(String json) {
        return objectMapper.readTree(json);
    }
}
