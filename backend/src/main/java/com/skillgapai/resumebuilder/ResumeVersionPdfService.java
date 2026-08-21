package com.skillgapai.resumebuilder;

import com.skillgapai.dto.CertificationEntry;
import com.skillgapai.dto.ContactInfo;
import com.skillgapai.dto.EducationEntry;
import com.skillgapai.dto.ExperienceEntry;
import com.skillgapai.dto.ProjectEntry;
import com.skillgapai.dto.ResumeVersionView;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Phase 22: renders a ResumeVersionView as one clean, single-column,
 * ATS-friendly PDF - plain text, no tables/columns/graphics, since
 * those are exactly what Phase 13's ATS Compatibility Analyzer flags as
 * parser-breaking. Built on PDFBox, same manual word-wrap/pagination
 * Writer approach as GapReportPdfService - kept as its own copy rather
 * than a shared base class, since the two documents' layouts don't
 * otherwise overlap.
 *
 * Every section but Contact is optional and simply skipped when empty,
 * so a mostly-blank resume renders as a short, clean document instead of
 * empty headings or stranded whitespace.
 */
@Service
public class ResumeVersionPdfService {

    private static final float MARGIN = 50;
    private static final float PAGE_WIDTH = PDRectangle.A4.getWidth();
    private static final float PAGE_HEIGHT = PDRectangle.A4.getHeight();
    private static final float CONTENT_WIDTH = PAGE_WIDTH - 2 * MARGIN;

    public byte[] generate(ResumeVersionView resume) {
        try (PDDocument document = new PDDocument()) {
            Writer w = new Writer(document);

            writeContact(w, resume.contact());

            if (resume.summary() != null && !resume.summary().isBlank()) {
                w.sectionTitle("Summary");
                w.body(resume.summary());
                w.gap(14);
            }

            if (resume.skills() != null && !resume.skills().isEmpty()) {
                w.sectionTitle("Skills");
                w.body(String.join(", ", resume.skills()));
                w.gap(14);
            }

            if (resume.experience() != null && !resume.experience().isEmpty()) {
                w.sectionTitle("Experience");
                for (ExperienceEntry entry : resume.experience()) {
                    writeExperience(w, entry);
                }
                w.gap(6);
            }

            if (resume.projects() != null && !resume.projects().isEmpty()) {
                w.sectionTitle("Projects");
                for (ProjectEntry entry : resume.projects()) {
                    writeProject(w, entry);
                }
                w.gap(6);
            }

            if (resume.education() != null && !resume.education().isEmpty()) {
                w.sectionTitle("Education");
                for (EducationEntry entry : resume.education()) {
                    writeEducation(w, entry);
                }
                w.gap(6);
            }

            if (resume.certifications() != null && !resume.certifications().isEmpty()) {
                w.sectionTitle("Certifications");
                for (CertificationEntry entry : resume.certifications()) {
                    writeCertification(w, entry);
                }
            }

            w.close();

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            document.save(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to generate resume PDF", e);
        }
    }

    private void writeContact(Writer w, ContactInfo contact) throws IOException {
        w.heading(contact.name(), 20);

        List<String> line1 = new ArrayList<>();
        addIfPresent(line1, contact.email());
        addIfPresent(line1, contact.phone());
        addIfPresent(line1, contact.location());
        if (!line1.isEmpty()) {
            w.body(String.join("  |  ", line1));
        }

        List<String> line2 = new ArrayList<>();
        addIfPresent(line2, contact.portfolioUrl());
        addIfPresent(line2, contact.githubUrl());
        addIfPresent(line2, contact.linkedinUrl());
        if (!line2.isEmpty()) {
            w.body(String.join("  |  ", line2));
        }

        w.gap(16);
    }

    private void writeExperience(Writer w, ExperienceEntry entry) throws IOException {
        w.keepTogether(14 + 12);
        String title = joinNonBlank(" — ", entry.role(), entry.company());
        w.itemTitle(title.isEmpty() ? "Untitled role" : title);
        if (entry.dates() != null && !entry.dates().isBlank()) {
            w.itemMeta(entry.dates());
        }
        if (entry.bullets() != null) {
            for (String bullet : entry.bullets()) {
                if (bullet != null && !bullet.isBlank()) {
                    w.bullet(bullet);
                }
            }
        }
        w.gap(8);
    }

    private void writeProject(Writer w, ProjectEntry entry) throws IOException {
        w.keepTogether(14 + 12);
        String title = entry.name() == null || entry.name().isBlank() ? "Untitled project" : entry.name();
        if (entry.tech() != null && !entry.tech().isBlank()) {
            title = title + "  [" + entry.tech() + "]";
        }
        w.itemTitle(title);
        if (entry.description() != null && !entry.description().isBlank()) {
            w.itemBody(entry.description());
        }
        if (entry.link() != null && !entry.link().isBlank()) {
            w.itemMeta(entry.link());
        }
        w.gap(8);
    }

    private void writeEducation(Writer w, EducationEntry entry) throws IOException {
        w.keepTogether(14 + 12);
        String title = joinNonBlank(" — ", entry.degree(), entry.institution());
        w.itemTitle(title.isEmpty() ? "Untitled" : title);
        List<String> meta = new ArrayList<>();
        addIfPresent(meta, entry.dates());
        if (entry.gpa() != null && !entry.gpa().isBlank()) {
            meta.add("GPA " + entry.gpa());
        }
        if (!meta.isEmpty()) {
            w.itemMeta(String.join("  |  ", meta));
        }
        w.gap(8);
    }

    private void writeCertification(Writer w, CertificationEntry entry) throws IOException {
        w.keepTogether(14);
        String title = joinNonBlank(" — ", entry.name(), entry.issuer());
        if (entry.date() != null && !entry.date().isBlank()) {
            title = title.isEmpty() ? entry.date() : title + " (" + entry.date() + ")";
        }
        w.itemTitle(title.isEmpty() ? "Untitled certification" : title);
        w.gap(6);
    }

    private void addIfPresent(List<String> target, String value) {
        if (value != null && !value.isBlank()) {
            target.add(value);
        }
    }

    private String joinNonBlank(String separator, String a, String b) {
        boolean hasA = a != null && !a.isBlank();
        boolean hasB = b != null && !b.isBlank();
        if (hasA && hasB) {
            return a + separator + b;
        }
        return hasA ? a : (hasB ? b : "");
    }

    /**
     * Stateful cursor over a PDDocument - see GapReportPdfService's
     * Writer for the full rationale (PDFBox has no flowed-text support
     * of its own, so this wraps to CONTENT_WIDTH and starts a new page
     * whenever the next line would cross the bottom margin).
     */
    private static final class Writer implements AutoCloseable {
        private final PDDocument document;
        private final PDFont regular = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
        private final PDFont bold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
        private PDPageContentStream stream;
        private float y;

        Writer(PDDocument document) throws IOException {
            this.document = document;
            newPage();
        }

        void heading(String text, float size) throws IOException {
            writeLine(text == null || text.isBlank() ? "Untitled" : text, bold, size, size * 1.3f, 0);
        }

        void sectionTitle(String text) throws IOException {
            keepTogether(16 + 6);
            writeLine(text, bold, 13, 16, 0);
            y -= 4;
        }

        void body(String text) throws IOException {
            wrapAndWrite(text, regular, 10, 13, 0);
        }

        void itemTitle(String text) throws IOException {
            wrapAndWrite(text, bold, 10.5f, 14, 0);
        }

        void itemBody(String text) throws IOException {
            wrapAndWrite(text, regular, 9.5f, 12, 14);
        }

        void itemMeta(String text) throws IOException {
            wrapAndWrite(text, regular, 9, 12, 14);
        }

        void bullet(String text) throws IOException {
            wrapAndWrite("-  " + text, regular, 9.5f, 12, 14);
        }

        void gap(float amount) {
            y -= amount;
        }

        void keepTogether(float minHeight) throws IOException {
            ensureSpace(minHeight);
        }

        private void wrapAndWrite(String text, PDFont font, float size, float leading, float indent) throws IOException {
            for (String line : wrap(text, font, size, CONTENT_WIDTH - indent)) {
                writeLine(line, font, size, leading, indent);
            }
        }

        private void writeLine(String text, PDFont font, float size, float leading, float indent) throws IOException {
            ensureSpace(leading);
            stream.beginText();
            stream.setFont(font, size);
            stream.newLineAtOffset(MARGIN + indent, y);
            stream.showText(text);
            stream.endText();
            y -= leading;
        }

        private void ensureSpace(float needed) throws IOException {
            if (y - needed < MARGIN) {
                newPage();
            }
        }

        private void newPage() throws IOException {
            if (stream != null) {
                stream.close();
            }
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);
            stream = new PDPageContentStream(document, page);
            y = PAGE_HEIGHT - MARGIN;
        }

        private List<String> wrap(String text, PDFont font, float size, float maxWidth) throws IOException {
            List<String> lines = new ArrayList<>();
            for (String paragraph : text.split("\n", -1)) {
                StringBuilder current = new StringBuilder();
                for (String word : paragraph.split(" ")) {
                    String candidate = current.isEmpty() ? word : current + " " + word;
                    if (!current.isEmpty() && font.getStringWidth(candidate) / 1000 * size > maxWidth) {
                        lines.add(current.toString());
                        current = new StringBuilder(word);
                    } else {
                        current = new StringBuilder(candidate);
                    }
                }
                lines.add(current.toString());
            }
            return lines;
        }

        @Override
        public void close() throws IOException {
            stream.close();
        }
    }
}
