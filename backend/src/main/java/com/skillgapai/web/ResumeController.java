package com.skillgapai.web;

import com.skillgapai.dto.ExtractedTextResponse;
import com.skillgapai.parsing.ResumeParsingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * Phase 5: turns an uploaded resume PDF into plain text so it can be fed
 * into POST /api/match, which still only accepts pasted text. This is
 * the "PDF" half of "PDF parsing with manual-paste fallback" - the
 * manual-paste half is just calling /api/match with typed/pasted text
 * directly and skipping this endpoint entirely.
 */
@RestController
@RequestMapping("/api/resumes")
public class ResumeController {

    private final ResumeParsingService parsingService;

    public ResumeController(ResumeParsingService parsingService) {
        this.parsingService = parsingService;
    }

    @PostMapping("/extract-text")
    public ResponseEntity<?> extractText(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(
                    "No file uploaded. Please attach a PDF, or call /api/match with pasted resume text instead.");
        }

        String text;
        try {
            text = parsingService.extractText(file.getBytes());
        } catch (IOException e) {
            return ResponseEntity.badRequest().body(
                    "Could not read this PDF - it may be corrupted or password-protected. "
                            + "Please try a different file, or paste your resume text manually instead.");
        }

        if (text.isBlank()) {
            return ResponseEntity.badRequest().body(
                    "No extractable text found in this PDF - it may be a scanned image with no text layer. "
                            + "Please paste your resume text manually instead.");
        }

        return ResponseEntity.ok(new ExtractedTextResponse(text, text.length()));
    }

    /**
     * Spring throws MultipartException before this controller's method
     * body even runs when the request has no "file" part at all (e.g. a
     * client that forgot to attach one) - without this handler that
     * surfaces as a 500, but a missing upload is a client mistake, not a
     * server failure.
     */
    @ExceptionHandler(MultipartException.class)
    public ResponseEntity<?> handleMissingFile() {
        return ResponseEntity.badRequest().body(
                "No file uploaded. Please attach a PDF, or call /api/match with pasted resume text instead.");
    }
}
