package com.skillgapai.export;

import com.skillgapai.dto.GapReportView;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Phase 9: renders a GapReportView as a PDF, mirroring what
 * GapReportScreen shows on screen - match percentage, then matched,
 * missing (numbered, easiest to close first), and underemphasized
 * skills, each with its reason. Built on PDFBox (already a project
 * dependency for resume parsing) rather than a new library: PDFBox has
 * no flowed-text/pagination support of its own, so the private Writer
 * below is just enough of one - word-wrap plus "start a new page when
 * this line would run off the bottom margin" - for a single-column
 * report.
 */
@Service
public class GapReportPdfService {

    private static final float MARGIN = 50;
    private static final float PAGE_WIDTH = PDRectangle.A4.getWidth();
    private static final float PAGE_HEIGHT = PDRectangle.A4.getHeight();
    private static final float CONTENT_WIDTH = PAGE_WIDTH - 2 * MARGIN;
    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("d MMM yyyy, h:mm a", Locale.ENGLISH);

    public byte[] generate(GapReportView report) {
        try (PDDocument document = new PDDocument()) {
            Writer writer = new Writer(document);
            writeHeader(writer, report);
            writeSection(writer, "Matched", null, report.matched(),
                    "None of the required skills were found in this resume.", false);
            writeSection(writer, "Missing", "Ranked easiest to close first", report.missing(),
                    "Every required skill was found - nothing missing.", true);
            writeSection(writer, "Underemphasized", "Present, but only mentioned once for a core requirement",
                    report.underemphasized(), "Nothing underemphasized.", false);
            writer.close();

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            document.save(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to generate report PDF", e);
        }
    }

    private void writeHeader(Writer w, GapReportView report) throws IOException {
        w.heading("SkillGap AI - Gap Report", 18);
        w.gap(10);

        int matchedCount = report.matched().size();
        int totalCount = matchedCount + report.missing().size();
        int rounded = (int) Math.round(report.matchPercentage());

        w.heading(rounded + "% match", 26);
        w.body(matchedCount + " of " + totalCount + " required skills found in your resume.");
        w.body("Generated " + report.createdAt().format(DATE_FORMAT));
        w.gap(18);
    }

    private void writeSection(Writer w, String title, String subtitle, JsonNode items,
                               String emptyText, boolean numbered) throws IOException {
        w.sectionTitle(title + " (" + items.size() + ")");
        if (subtitle != null) {
            w.subtitle(subtitle);
        }
        w.gap(6);

        if (items.isEmpty()) {
            w.body(emptyText);
        } else {
            int index = 1;
            for (JsonNode item : items) {
                // Reserve room for the title line plus at least the first
                // line of its reason before starting, so a skill name
                // never ends up alone at the bottom of a page with its
                // reason stranded on the next one.
                w.keepTogether(14 + 12);
                w.itemTitle((numbered ? (index++) + ". " : "") + itemLabel(item));
                w.itemBody(item.path("reason").asText(""));
                w.gap(8);
            }
        }
        w.gap(16);
    }

    private String itemLabel(JsonNode item) {
        String skillName = item.path("skillName").asText();
        String matchType = item.path("matchType").asText(null);
        String difficulty = item.path("difficulty").asText("");
        int occurrenceCount = item.path("occurrenceCount").asInt(0);

        List<String> tags = new ArrayList<>();
        if (matchType != null && !matchType.equals("NOT_FOUND")) {
            tags.add(matchTypeLabel(matchType));
        }
        if (!difficulty.isBlank()) {
            tags.add(difficulty.toLowerCase(Locale.ROOT));
        }
        if (occurrenceCount > 1) {
            tags.add("mentioned " + occurrenceCount + " times");
        }

        return tags.isEmpty() ? skillName : skillName + "  [" + String.join(", ", tags) + "]";
    }

    private String matchTypeLabel(String matchType) {
        return switch (matchType) {
            case "EXACT" -> "exact match";
            case "SYNONYM" -> "synonym match";
            case "FUZZY" -> "possible typo";
            default -> matchType.toLowerCase(Locale.ROOT);
        };
    }

    /**
     * Stateful cursor over a PDDocument: tracks the current page/content
     * stream and y-position, wraps text to CONTENT_WIDTH, and starts a
     * new page whenever the next line would cross the bottom margin.
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
            writeLine(text, bold, size, size * 1.3f, 0);
        }

        void sectionTitle(String text) throws IOException {
            writeLine(text, bold, 13, 16, 0);
        }

        void subtitle(String text) throws IOException {
            writeLine(text, regular, 9, 12, 0);
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
