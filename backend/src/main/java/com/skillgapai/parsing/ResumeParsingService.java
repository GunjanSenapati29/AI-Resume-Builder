package com.skillgapai.parsing;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * Phase 5: extracts plain text from an uploaded resume PDF using Apache
 * PDFBox. Deliberately narrow - one method, one job - so ResumeController
 * decides what counts as a usable result (e.g. an empty extraction from
 * a scanned/image-only PDF) rather than this class guessing at HTTP
 * behavior.
 */
@Service
public class ResumeParsingService {

    public String extractText(byte[] pdfBytes) throws IOException {
        try (PDDocument document = Loader.loadPDF(pdfBytes)) {
            return new PDFTextStripper().getText(document);
        }
    }
}
