package com.skillgapai.web;

import com.skillgapai.dto.GapReportSummary;
import com.skillgapai.dto.GapReportView;
import com.skillgapai.dto.LearningRoadmapView;
import com.skillgapai.export.GapReportPdfService;
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
import org.springframework.web.bind.annotation.RestController;

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
 */
@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;
    private final GapReportPdfService pdfService;
    private final LearningRoadmapService learningRoadmapService;

    public ReportController(ReportService reportService, GapReportPdfService pdfService,
                             LearningRoadmapService learningRoadmapService) {
        this.reportService = reportService;
        this.pdfService = pdfService;
        this.learningRoadmapService = learningRoadmapService;
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

    private String currentUserEmail() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}
