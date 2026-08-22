package com.skillgapai.web;

import com.skillgapai.career.CareerRoleService;
import com.skillgapai.dto.DashboardSummaryView;
import com.skillgapai.dto.GapReportSummary;
import com.skillgapai.dto.GapReportView;
import com.skillgapai.dto.InterviewQuestionsView;
import com.skillgapai.dto.LearningRoadmapView;
import com.skillgapai.dto.ProgressPointView;
import com.skillgapai.dto.RoleRecommendationsView;
import com.skillgapai.export.GapReportPdfService;
import com.skillgapai.interview.InterviewQuestionService;
import com.skillgapai.roadmap.LearningRoadmapService;
import com.skillgapai.service.ReportService;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Phase 4: GET /api/reports/{id} - looks up a previously saved GapReport
 * and returns it in the same shape POST /api/match returns right after
 * creating one.
 *
 * Phase 6d: GET /api/reports - lists past reports (most recent first)
 * for the History screen.
 *
 * Phase 6e: both routes are scoped to the logged-in user (from the JWT,
 * via SecurityContextHolder) - one account never sees another's history
 * or reports.
 *
 * Phase 9: GET /api/reports/{id}/pdf - the same report, rendered as a
 * downloadable PDF. Reuses getReport's ownership check below rather than
 * duplicating it.
 *
 * Phase 17: GET /api/reports/latest/learning-roadmap - the Learning
 * Roadmap for the user's single most recent report. 204 (not 404) when
 * the user has no analyzed reports yet, since "no reports" isn't an
 * error - the frontend renders a normal empty state for it, same as
 * HistoryScreen does for an empty report list.
 *
 * Phase 19: GET /api/reports/latest/interview-questions - same 204
 * pattern, but built from the same latest report's MATCHED skills
 * instead of missing/underemphasized.
 *
 * Phase 20: GET /api/reports/role-recommendations - same 204 pattern,
 * but aggregated across ALL of the user's reports rather than just the
 * latest one, so it lives outside the /latest/... group above.
 *
 * Phase 21: GET /api/reports/compare?ids=1,2,3 - 2 to 5 reports, side by
 * side. 400 for a malformed/out-of-range ids param, 404 (not 403, same
 * reasoning as getReport) if ANY id doesn't resolve to one of the
 * logged-in user's own reports.
 *
 * Phase 24: GET /api/reports/dashboard-summary and
 * /api/reports/progress-trend - read-only aggregations of data every
 * GapReport already stores, same 204-means-no-reports-yet convention as
 * Learning Roadmap/Interview Prep above.
 */
@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;
    private final GapReportPdfService pdfService;
    private final LearningRoadmapService learningRoadmapService;
    private final InterviewQuestionService interviewQuestionService;
    private final CareerRoleService careerRoleService;

    public ReportController(ReportService reportService, GapReportPdfService pdfService,
                             LearningRoadmapService learningRoadmapService,
                             InterviewQuestionService interviewQuestionService,
                             CareerRoleService careerRoleService) {
        this.reportService = reportService;
        this.pdfService = pdfService;
        this.learningRoadmapService = learningRoadmapService;
        this.interviewQuestionService = interviewQuestionService;
        this.careerRoleService = careerRoleService;
    }

    @GetMapping
    public List<GapReportSummary> listReports() {
        return reportService.listReportHistory(currentUserEmail());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getReport(@PathVariable Long id) {
        return reportService.getReport(id, currentUserEmail())
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> getReportPdf(@PathVariable Long id) {
        Optional<GapReportView> report = reportService.getReport(id, currentUserEmail());
        if (report.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        byte[] pdf = pdfService.generate(report.get());
        String filename = "gap-report-" + id + ".pdf";

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(filename).build().toString())
                .body(pdf);
    }

    @GetMapping("/latest/learning-roadmap")
    public ResponseEntity<LearningRoadmapView> getLatestLearningRoadmap() {
        return learningRoadmapService.buildForLatestReport(currentUserEmail())
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @GetMapping("/latest/interview-questions")
    public ResponseEntity<InterviewQuestionsView> getLatestInterviewQuestions() {
        return interviewQuestionService.buildForLatestReport(currentUserEmail())
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @GetMapping("/role-recommendations")
    public ResponseEntity<RoleRecommendationsView> getRoleRecommendations() {
        return careerRoleService.buildForAllReports(currentUserEmail())
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @GetMapping("/compare")
    public ResponseEntity<?> compareReports(@RequestParam String ids) {
        List<Long> parsedIds;
        try {
            parsedIds = parseIds(ids);
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body("ids must be a comma-separated list of report IDs.");
        }
        if (parsedIds.size() < 2 || parsedIds.size() > 5) {
            return ResponseEntity.badRequest().body("Select between 2 and 5 reports to compare.");
        }

        return reportService.compareReports(parsedIds, currentUserEmail())
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/dashboard-summary")
    public ResponseEntity<DashboardSummaryView> getDashboardSummary() {
        return reportService.getDashboardSummary(currentUserEmail())
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @GetMapping("/progress-trend")
    public ResponseEntity<List<ProgressPointView>> getProgressTrend() {
        return reportService.getProgressTrend(currentUserEmail())
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    private List<Long> parseIds(String ids) {
        List<Long> parsed = new ArrayList<>();
        for (String part : ids.split(",")) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty()) {
                parsed.add(Long.parseLong(trimmed));
            }
        }
        return parsed;
    }

    private String currentUserEmail() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}
