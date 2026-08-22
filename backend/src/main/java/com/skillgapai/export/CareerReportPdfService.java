package com.skillgapai.export;

import com.skillgapai.dto.InterviewQuestionsView;
import com.skillgapai.dto.JobReadinessView;
import com.skillgapai.dto.LearningRoadmapView;
import com.skillgapai.dto.SkillInterviewQuestionsView;
import com.skillgapai.dto.SkillRoadmapItemView;
import com.skillgapai.entity.GapReport;
import com.skillgapai.interview.InterviewQuestionService;
import com.skillgapai.model.JobReadinessLabel;
import com.skillgapai.model.PriorityTier;
import com.skillgapai.repository.GapReportRepository;
import com.skillgapai.roadmap.LearningRoadmapService;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Phase 25: combines three EXISTING per-report views - the persisted Job
 * Readiness Score/breakdown (Phase 16, read straight off the GapReport
 * row, same fields ReportService.toJobReadinessView(GapReport) already
 * exposes), LearningRoadmapService's live roadmap (Phase 17/18), and
 * InterviewQuestionService's live interview questions (Phase 19) - into
 * one combined PDF, all three already scoped to the user's single most
 * recent report. None of those three services' own logic changes here;
 * this only reads what they already produce and lays it out.
 *
 * Same manual word-wrap/pagination PDFBox technique GapReportPdfService
 * established in Phase 9 - kept as its own copy rather than a shared
 * Writer, same reasoning Phase 22's ResumeVersionPdfService already used
 * for a differently-shaped document (this one has its own section
 * structure - score breakdown, then skill gaps, then roadmap, then
 * interview questions - that doesn't overlap with the single-report Gap
 * Report layout).
 */
@Service
public class CareerReportPdfService {

    private static final float MARGIN = 50;
    private static final float PAGE_WIDTH = PDRectangle.A4.getWidth();
    private static final float PAGE_HEIGHT = PDRectangle.A4.getHeight();
    private static final float CONTENT_WIDTH = PAGE_WIDTH - 2 * MARGIN;
    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("d MMM yyyy, h:mm a", Locale.ENGLISH);

    private final GapReportRepository gapReportRepository;
    private final LearningRoadmapService learningRoadmapService;
    private final InterviewQuestionService interviewQuestionService;

    public CareerReportPdfService(GapReportRepository gapReportRepository,
                                   LearningRoadmapService learningRoadmapService,
                                   InterviewQuestionService interviewQuestionService) {
        this.gapReportRepository = gapReportRepository;
        this.learningRoadmapService = learningRoadmapService;
        this.interviewQuestionService = interviewQuestionService;
    }

    /**
     * Empty when the user has no analyzed reports yet - same "no reports
     * yet isn't an error" convention as every other /latest/... endpoint,
     * just surfaced as an empty Optional instead of a 204 since this
     * returns bytes rather than JSON (see ReportController).
     */
    @Transactional(readOnly = true)
    public Optional<byte[]> generateForLatestReport(String userEmail) {
        Optional<GapReport> latestReport = gapReportRepository.findFirstByResume_User_EmailOrderByCreatedAtDesc(userEmail);
        if (latestReport.isEmpty()) {
            return Optional.empty();
        }
        GapReport report = latestReport.get();

        // Guaranteed present: both are scoped to the same "most recent
        // report" we just confirmed exists above.
        LearningRoadmapView roadmap = learningRoadmapService.buildForLatestReport(userEmail).orElseThrow();
        InterviewQuestionsView interview = interviewQuestionService.buildForLatestReport(userEmail).orElseThrow();

        return Optional.of(render(report, roadmap, interview));
    }

    private byte[] render(GapReport report, LearningRoadmapView roadmap, InterviewQuestionsView interview) {
        try (PDDocument document = new PDDocument()) {
            Writer writer = new Writer(document);
            writeHeader(writer, report.getCreatedAt());
            writeReadinessSection(writer, toReadinessView(report));
            writeSkillGapsSection(writer, roadmap.missingSkills());
            writeRoadmapSection(writer, roadmap);
            writeInterviewSection(writer, interview);
            writer.close();

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            document.save(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to generate career report PDF", e);
        }
    }

    private JobReadinessView toReadinessView(GapReport report) {
        return new JobReadinessView(report.getJobReadinessScore(), report.getJobReadinessLabel(),
                (int) Math.round(report.getMatchPercentage()), report.getAtsScore(),
                report.getEvidenceStrengthScore(), report.getGapSeverityScore());
    }

    private void writeHeader(Writer w, LocalDateTime reportCreatedAt) throws IOException {
        w.heading("SkillGap AI - Career Report", 18);
        w.gap(6);
        w.body("Based on your most recent analysis, generated " + reportCreatedAt.format(DATE_FORMAT));
        w.gap(18);
    }

    private void writeReadinessSection(Writer w, JobReadinessView readiness) throws IOException {
        w.sectionTitle("Job Readiness Score");
        w.gap(6);
        w.heading(readiness.overallScore() + "/100 - " + labelText(readiness.label()), 16);
        w.gap(4);
        w.body("Skill Match (40%): " + readiness.skillMatchScore());
        w.body("ATS Compatibility (20%): " + readiness.atsScore());
        w.body("Evidence Strength (20%): " + readiness.evidenceStrengthScore());
        w.body("Gap Severity (20%): " + readiness.gapSeverityScore());
        w.gap(16);
    }

    private String labelText(JobReadinessLabel label) {
        return switch (label) {
            case EXCELLENT -> "Excellent";
            case STRONG -> "Strong";
            case NEEDS_WORK -> "Needs Work";
            case NOT_READY -> "Not Ready";
        };
    }

    private void writeSkillGapsSection(Writer w, List<SkillRoadmapItemView> missingSkills) throws IOException {
        w.sectionTitle("Skill Gaps (" + missingSkills.size() + ")");
        w.subtitle("Ranked by priority - Critical, then Important, then Optional");
        w.gap(6);

        if (missingSkills.isEmpty()) {
            w.body("Every required skill was found - nothing missing.");
        } else {
            for (SkillRoadmapItemView item : missingSkills) {
                w.keepTogether(14);
                w.itemTitle(item.skillName() + "  [" + tierText(item.priorityTier()) + "]");
            }
        }
        w.gap(16);
    }

    private String tierText(PriorityTier tier) {
        if (tier == null) {
            return "unranked";
        }
        return switch (tier) {
            case CRITICAL -> "Critical";
            case IMPORTANT -> "Important";
            case OPTIONAL -> "Optional";
        };
    }

    private void writeRoadmapSection(Writer w, LearningRoadmapView roadmap) throws IOException {
        w.sectionTitle("Learning Roadmap");
        w.gap(6);

        writeRoadmapItems(w, "Missing skills", roadmap.missingSkills(),
                "Every required skill was found - nothing missing.");
        writeRoadmapItems(w, "Underemphasized skills", roadmap.underemphasizedSkills(),
                "Nothing underemphasized.");
    }

    private void writeRoadmapItems(Writer w, String subtitle, List<SkillRoadmapItemView> items, String emptyText) throws IOException {
        w.subtitle(subtitle);
        w.gap(6);

        if (items.isEmpty()) {
            w.body(emptyText);
        } else {
            for (SkillRoadmapItemView item : items) {
                w.keepTogether(14 + 12);
                w.itemTitle(item.skillName());
                int step = 1;
                for (String line : item.steps()) {
                    w.itemBody(step++ + ". " + line);
                }
                if (item.officialDocUrl() != null) {
                    w.itemBody("Official docs: " + item.officialDocUrl());
                }
                w.gap(8);
            }
        }
        w.gap(12);
    }

    private void writeInterviewSection(Writer w, InterviewQuestionsView interview) throws IOException {
        w.sectionTitle("Interview Prep (" + interview.skills().size() + ")");
        w.subtitle("Practice questions for the skills matched on your most recent report");
        w.gap(6);

        if (interview.skills().isEmpty()) {
            w.body("No matched skills to build interview questions from yet.");
        } else {
            for (SkillInterviewQuestionsView skill : interview.skills()) {
                w.keepTogether(14 + 12);
                w.itemTitle(skill.skillName());
                int index = 1;
                for (String question : skill.questions()) {
                    w.itemBody(index++ + ". " + question);
                }
                w.gap(8);
            }
        }
    }

    /**
     * Stateful cursor over a PDDocument - identical wrap/paginate approach
     * to GapReportPdfService.Writer (see that class for the reasoning),
     * kept as its own copy since the two PDFs' section layouts differ.
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
