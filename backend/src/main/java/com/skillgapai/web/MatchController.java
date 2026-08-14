package com.skillgapai.web;

import com.skillgapai.dto.MatchRequest;
import com.skillgapai.matching.SkillMatchingService;
import com.skillgapai.model.GapAnalysisResult;
import com.skillgapai.model.RequiredSkill;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Phase 3: exposes the Phase 1 matching logic over HTTP. No persistence
 * yet (that's Phase 4) - this just proves Spring MVC can call
 * SkillMatchingService end-to-end and return a real JSON gap report.
 */
@RestController
@RequestMapping("/api")
public class MatchController {

    private final SkillMatchingService matchingService;

    public MatchController(SkillMatchingService matchingService) {
        this.matchingService = matchingService;
    }

    @PostMapping("/match")
    public ResponseEntity<?> match(@RequestBody MatchRequest request) {
        if (request.resumeText() == null || request.resumeText().isBlank()) {
            return ResponseEntity.badRequest().body("resumeText is required.");
        }
        if (request.requiredSkills() == null || request.requiredSkills().isEmpty()) {
            return ResponseEntity.badRequest().body("requiredSkills must contain at least one skill.");
        }

        List<RequiredSkill> requiredSkills = request.requiredSkills().stream()
                .map(rs -> new RequiredSkill(rs.skillName(), rs.core()))
                .toList();

        GapAnalysisResult result = matchingService.analyze(request.resumeText(), requiredSkills);
        return ResponseEntity.ok(result);
    }
}
