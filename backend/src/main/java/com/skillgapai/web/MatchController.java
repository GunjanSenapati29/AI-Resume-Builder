package com.skillgapai.web;

import com.skillgapai.dto.GapReportView;
import com.skillgapai.dto.MatchRequest;
import com.skillgapai.model.RequiredSkill;
import com.skillgapai.service.ReportService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

/**
 * Phase 3 exposed the matching logic over HTTP with no persistence.
 * Phase 4 wires in ReportService so every match run is now also saved
 * as a GapReport row - the response is unchanged in spirit (still the
 * matched/missing/underemphasized/matchPercentage breakdown), just now
 * wrapped in a GapReportView that also carries the saved report's id.
 */
@RestController
@RequestMapping("/api")
public class MatchController {

    private final ReportService reportService;

    public MatchController(ReportService reportService) {
        this.reportService = reportService;
    }

    @PostMapping("/match")
    public ResponseEntity<?> match(@RequestBody MatchRequest request) {
        if (request.resumeText() == null || request.resumeText().isBlank()) {
            return ResponseEntity.badRequest().body("resumeText is required.");
        }
        if (request.jdText() == null || request.jdText().isBlank()) {
            return ResponseEntity.badRequest().body("jdText is required.");
        }
        if (request.requiredSkills() == null || request.requiredSkills().isEmpty()) {
            return ResponseEntity.badRequest().body("requiredSkills must contain at least one skill.");
        }

        List<RequiredSkill> requiredSkills = request.requiredSkills().stream()
                .map(rs -> new RequiredSkill(rs.skillName(), rs.core()))
                .toList();

        GapReportView view = reportService.createReport(request.resumeText(), request.jdText(), requiredSkills);
        return ResponseEntity.created(URI.create("/api/reports/" + view.id())).body(view);
    }
}
