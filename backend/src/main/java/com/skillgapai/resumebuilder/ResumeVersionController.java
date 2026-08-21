package com.skillgapai.resumebuilder;

import com.skillgapai.dto.ResumeVersionCompareRequest;
import com.skillgapai.dto.ResumeVersionRequest;
import com.skillgapai.dto.ResumeVersionSummary;
import com.skillgapai.dto.ResumeVersionView;
import com.skillgapai.model.RequiredSkill;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

/**
 * Phase 22: CRUD + duplicate + PDF export for ResumeVersion, all scoped
 * to the logged-in user (from the JWT, via SecurityContextHolder) - same
 * 404-not-403 ownership pattern ReportController already uses, so a
 * request for another user's resume version never confirms or denies
 * that the id belongs to someone else's account.
 *
 * Phase 23: POST /compare runs the same ownership-scoped lookup for
 * exactly 2 resume version ids, then hands off to
 * ResumeVersionComparisonService - computed live, never persisted.
 */
@RestController
@RequestMapping("/api/resume-versions")
public class ResumeVersionController {

    private static final int COMPARE_COUNT = 2;

    private final ResumeVersionService resumeVersionService;
    private final ResumeVersionPdfService pdfService;
    private final ResumeVersionComparisonService comparisonService;

    public ResumeVersionController(ResumeVersionService resumeVersionService, ResumeVersionPdfService pdfService,
                                    ResumeVersionComparisonService comparisonService) {
        this.resumeVersionService = resumeVersionService;
        this.pdfService = pdfService;
        this.comparisonService = comparisonService;
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody ResumeVersionRequest request) {
        String error = validate(request);
        if (error != null) {
            return ResponseEntity.badRequest().body(error);
        }
        return ResponseEntity.ok(resumeVersionService.create(currentUserEmail(), request));
    }

    @GetMapping
    public List<ResumeVersionSummary> list() {
        return resumeVersionService.list(currentUserEmail());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable Long id) {
        return resumeVersionService.get(id, currentUserEmail())
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody ResumeVersionRequest request) {
        String error = validate(request);
        if (error != null) {
            return ResponseEntity.badRequest().body(error);
        }
        return resumeVersionService.update(id, currentUserEmail(), request)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        boolean deleted = resumeVersionService.delete(id, currentUserEmail());
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    @PostMapping("/{id}/duplicate")
    public ResponseEntity<?> duplicate(@PathVariable Long id) {
        return resumeVersionService.duplicate(id, currentUserEmail())
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> getPdf(@PathVariable Long id) {
        Optional<ResumeVersionView> version = resumeVersionService.get(id, currentUserEmail());
        if (version.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        byte[] pdf = pdfService.generate(version.get());
        String filename = "resume-" + id + ".pdf";

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(filename).build().toString())
                .body(pdf);
    }

    @PostMapping("/compare")
    public ResponseEntity<?> compare(@RequestBody ResumeVersionCompareRequest request) {
        if (request.resumeVersionIds() == null || request.resumeVersionIds().size() != COMPARE_COUNT) {
            return ResponseEntity.badRequest().body("Select exactly 2 resume versions to compare.");
        }
        if (isBlank(request.jdText())) {
            return ResponseEntity.badRequest().body("jdText is required.");
        }
        if (request.requiredSkills() == null || request.requiredSkills().isEmpty()) {
            return ResponseEntity.badRequest().body("requiredSkills must contain at least one skill.");
        }

        List<RequiredSkill> requiredSkills = request.requiredSkills().stream()
                .map(rs -> new RequiredSkill(rs.skillName(), rs.core()))
                .toList();

        return comparisonService.compare(request.resumeVersionIds(), currentUserEmail(), request.jdText(), requiredSkills)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    private String validate(ResumeVersionRequest request) {
        if (isBlank(request.title())) {
            return "title is required.";
        }
        if (request.contact() == null || isBlank(request.contact().name())) {
            return "contact.name is required.";
        }
        return null;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private String currentUserEmail() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}
