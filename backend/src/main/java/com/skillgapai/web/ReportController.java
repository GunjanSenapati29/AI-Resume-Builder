package com.skillgapai.web;

import com.skillgapai.dto.GapReportSummary;
import com.skillgapai.dto.GapReportView;
import com.skillgapai.export.GapReportPdfService;
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
 */
@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;
    private final GapReportPdfService pdfService;

    public ReportController(ReportService reportService, GapReportPdfService pdfService) {
        this.reportService = reportService;
        this.pdfService = pdfService;
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

    private String currentUserEmail() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}
