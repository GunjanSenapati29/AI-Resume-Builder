package com.skillgapai.web;

import com.skillgapai.dto.GapReportSummary;
import com.skillgapai.service.ReportService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Phase 4: GET /api/reports/{id} - looks up a previously saved GapReport
 * and returns it in the same shape POST /api/match returns right after
 * creating one.
 *
 * Phase 6d: GET /api/reports - lists past reports (most recent first)
 * for the History screen.
 */
@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping
    public List<GapReportSummary> listReports() {
        return reportService.listReportHistory();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getReport(@PathVariable Long id) {
        return reportService.getReport(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
