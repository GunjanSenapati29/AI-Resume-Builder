package com.skillgapai.ats;

import com.skillgapai.model.AtsAnalysisResult;
import com.skillgapai.model.AtsCheckResult;
import com.skillgapai.model.AtsSeverity;
import com.skillgapai.model.ResumeSectionBlocks;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Phase 13: rule-based ATS (Applicant Tracking System) compatibility
 * checks, run against the same extracted resume text the skill-matching
 * analysis uses. Every check is plain Java string/regex logic - no ML,
 * consistent with the project's "transparent, rule-based" matching
 * approach (see CLAUDE.md).
 *
 * Five checks run every time, in this fixed order: contact info,
 * section headers, extractable text, length, structure. Each always
 * produces exactly one AtsCheckResult - PASSED ones too - so the report
 * can show "here's every check we ran", not just the failures.
 */
@Service
public class AtsAnalyzerService {

    private static final int FIRST_LINES_FOR_CONTACT_INFO = 15;
    private static final int MIN_EXTRACTABLE_WORDS = 40;
    private static final int MIN_REASONABLE_WORDS = 150;
    private static final int MAX_REASONABLE_WORDS = 1200;
    private static final int MIN_BULLET_LINES = 3;
    private static final int MIN_LINES_FOR_PARAGRAPH_CHECK = 8;
    private static final int MAX_AVERAGE_LINE_LENGTH = 120;

    private static final int MEDIUM_DEDUCTION = 15;
    private static final int HIGH_DEDUCTION = 30;

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}");

    // Not a strict phone-number validator - just "a run of digits, with
    // optional +/space/-/./() separators, long enough to plausibly be a
    // phone number (9-15 digit characters)". Good enough for a resume
    // scan; a real phone-number library would be overkill here.
    private static final Pattern PHONE_PATTERN =
            Pattern.compile("(\\+?\\d[\\d\\-.\\s()]{7,17}\\d)");

    private static final Pattern BULLET_LINE_PATTERN =
            Pattern.compile("^[•●‣◦▪○*\\-]\\s+.+");

    private static final String[] REQUIRED_HEADINGS = {"Experience", "Education", "Skills", "Projects"};
    private static final int MAX_HEADING_LINE_WORDS = 6;

    public AtsAnalysisResult analyze(String resumeText) {
        String text = resumeText == null ? "" : resumeText;
        String[] lines = text.split("\n", -1);
        int wordCount = countWords(text);

        List<AtsCheckResult> checks = new ArrayList<>();
        checks.add(checkContactInfo(lines));
        checks.add(checkSectionHeaders(lines));
        checks.add(checkExtractableText(wordCount));
        checks.add(checkLength(wordCount));
        checks.add(checkStructure(lines));

        int score = 100;
        for (AtsCheckResult check : checks) {
            score -= deductionFor(check.getSeverity());
        }
        score = Math.max(0, Math.min(100, score));

        return new AtsAnalysisResult(score, checks);
    }

    private int deductionFor(AtsSeverity severity) {
        return switch (severity) {
            case HIGH -> HIGH_DEDUCTION;
            case MEDIUM -> MEDIUM_DEDUCTION;
            case PASSED -> 0;
        };
    }

    private int countWords(String text) {
        String trimmed = text.strip();
        if (trimmed.isEmpty()) {
            return 0;
        }
        return trimmed.split("\\s+").length;
    }

    private AtsCheckResult checkContactInfo(String[] lines) {
        int limit = Math.min(FIRST_LINES_FOR_CONTACT_INFO, lines.length);
        String head = String.join("\n", java.util.Arrays.copyOfRange(lines, 0, limit));

        boolean hasEmail = EMAIL_PATTERN.matcher(head).find();
        boolean hasPhone = PHONE_PATTERN.matcher(head).find();

        if (hasEmail && hasPhone) {
            return new AtsCheckResult(
                    "Contact Info Found",
                    "An email address and phone number were both found near the top of your resume.",
                    null,
                    AtsSeverity.PASSED);
        }

        if (hasEmail || hasPhone) {
            String missing = hasEmail ? "phone number" : "email address";
            return new AtsCheckResult(
                    "Contact Info Incomplete",
                    "A " + missing + " could not be found in the first part of your resume.",
                    "Add your " + missing + " near the top of your resume, in your header or contact block.",
                    AtsSeverity.MEDIUM);
        }

        return new AtsCheckResult(
                "Contact Info Missing",
                "No email address or phone number could be found near the top of your resume.",
                "Add a clear contact block (email and phone number) at the very top of your resume "
                        + "so an ATS and recruiters can find it immediately.",
                AtsSeverity.HIGH);
    }

    private AtsCheckResult checkSectionHeaders(String[] lines) {
        Set<String> found = findHeadingLineIndices(lines).keySet();

        int foundCount = found.size();
        if (foundCount == REQUIRED_HEADINGS.length) {
            return new AtsCheckResult(
                    "Section Headers Found",
                    "All four standard section headers (Experience, Education, Skills, Projects) were found.",
                    null,
                    AtsSeverity.PASSED);
        }

        List<String> missing = new ArrayList<>(List.of(REQUIRED_HEADINGS));
        missing.removeAll(found);
        String missingList = String.join(", ", missing);
        AtsSeverity severity = foundCount >= REQUIRED_HEADINGS.length - 1 ? AtsSeverity.MEDIUM : AtsSeverity.HIGH;

        return new AtsCheckResult(
                "Section Headers Missing",
                "Could not find a clearly labeled section for: " + missingList + ".",
                "Add a standard heading (on its own line) for each missing section - " + missingList
                        + " - so ATS parsers can identify and bucket that content correctly.",
                severity);
    }

    /**
     * The heading-detection loop checkSectionHeaders() uses to decide
     * presence/absence - factored out so Phase 14's extractSectionBlocks()
     * can reuse the exact same detection (same regex, same "first line it
     * appears on wins") instead of re-implementing it. Returns each found
     * heading mapped to the line index it was first seen on.
     */
    private Map<String, Integer> findHeadingLineIndices(String[] lines) {
        Map<String, Integer> indices = new LinkedHashMap<>();
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].strip();
            if (line.isEmpty() || line.split("\\s+").length > MAX_HEADING_LINE_WORDS) {
                continue;
            }
            for (String heading : REQUIRED_HEADINGS) {
                if (!indices.containsKey(heading)
                        && Pattern.compile("(?i)\\b" + heading + "\\b").matcher(line).find()) {
                    indices.put(heading, i);
                }
            }
        }
        return indices;
    }

    /**
     * Phase 14: slices the resume's Skills/Experience/Projects section
     * text out of the whole resume, using the same heading positions
     * checkSectionHeaders() finds. Each section's text runs from the line
     * after its heading up to whichever of the four headings comes next
     * (or the end of the resume) - so SkillEvidenceService can check
     * whether a skill is mentioned specifically within one section,
     * rather than just somewhere in the resume as a whole. A heading that
     * wasn't found yields an empty block for that section.
     */
    public ResumeSectionBlocks extractSectionBlocks(String resumeText) {
        String text = resumeText == null ? "" : resumeText;
        String[] lines = text.split("\n", -1);

        Map<String, Integer> headingLineIndex = findHeadingLineIndices(lines);
        List<Map.Entry<String, Integer>> orderedHeadings = new ArrayList<>(headingLineIndex.entrySet());
        orderedHeadings.sort(Map.Entry.comparingByValue());

        Map<String, String> blocks = new HashMap<>();
        for (int i = 0; i < orderedHeadings.size(); i++) {
            String heading = orderedHeadings.get(i).getKey();
            int start = Math.min(orderedHeadings.get(i).getValue() + 1, lines.length);
            int end = (i + 1 < orderedHeadings.size()) ? orderedHeadings.get(i + 1).getValue() : lines.length;
            end = Math.max(start, end);
            blocks.put(heading, String.join("\n", Arrays.copyOfRange(lines, start, end)));
        }

        return new ResumeSectionBlocks(
                blocks.getOrDefault("Skills", ""),
                blocks.getOrDefault("Experience", ""),
                blocks.getOrDefault("Projects", ""));
    }

    // /api/match only ever receives already-extracted text, never the
    // original PDF's bytes or page count (ResumeController discards the
    // file after extraction, and already rejects a fully blank
    // extraction upstream before this is ever reached) - so this
    // approximates "suspiciously little text for the file" with an
    // absolute low-word-count floor instead of a page/byte-size ratio.
    private AtsCheckResult checkExtractableText(int wordCount) {
        if (wordCount < MIN_EXTRACTABLE_WORDS) {
            return new AtsCheckResult(
                    "Text May Not Be Machine-Readable",
                    "Only " + wordCount + " words of extractable text were found, which usually means this PDF "
                            + "is a scanned image or has text embedded as pictures rather than real, selectable text.",
                    "Export your resume as a native text-based PDF (e.g. \"Save/Print as PDF\" from Word or "
                            + "Google Docs) rather than scanning a printed copy, or paste your resume text manually.",
                    AtsSeverity.HIGH);
        }

        return new AtsCheckResult(
                "Text Is Machine-Readable",
                "Your resume's text layer contains a normal amount of extractable text (" + wordCount + " words).",
                null,
                AtsSeverity.PASSED);
    }

    private AtsCheckResult checkLength(int wordCount) {
        if (wordCount < MIN_REASONABLE_WORDS) {
            return new AtsCheckResult(
                    "Resume Is Short",
                    "This resume is about " + wordCount + " words, which is on the shorter side for a professional resume.",
                    "Add more detail to your Experience and Projects sections - specific responsibilities, "
                            + "tools used, and measurable outcomes.",
                    AtsSeverity.MEDIUM);
        }

        if (wordCount > MAX_REASONABLE_WORDS) {
            return new AtsCheckResult(
                    "Resume Is Long",
                    "This resume is about " + wordCount + " words, longer than what most recruiters and ATS parsers expect.",
                    "Trim to your most relevant, recent experience - aim for roughly 400-800 words for most roles.",
                    AtsSeverity.MEDIUM);
        }

        return new AtsCheckResult(
                "Resume Length Is Reasonable",
                "At " + wordCount + " words, this resume is a reasonable length.",
                null,
                AtsSeverity.PASSED);
    }

    private AtsCheckResult checkStructure(String[] lines) {
        List<String> nonBlankLines = new ArrayList<>();
        int bulletLineCount = 0;
        long totalLength = 0;

        for (String rawLine : lines) {
            String line = rawLine.strip();
            if (line.isEmpty()) {
                continue;
            }
            nonBlankLines.add(line);
            totalLength += line.length();
            if (BULLET_LINE_PATTERN.matcher(line).matches()) {
                bulletLineCount++;
            }
        }

        double averageLineLength = nonBlankLines.isEmpty() ? 0 : (double) totalLength / nonBlankLines.size();
        boolean readsAsShortLines = nonBlankLines.size() >= MIN_LINES_FOR_PARAGRAPH_CHECK
                && averageLineLength <= MAX_AVERAGE_LINE_LENGTH;

        if (bulletLineCount >= MIN_BULLET_LINES || readsAsShortLines) {
            String detail = bulletLineCount >= MIN_BULLET_LINES
                    ? "Bullet points were found (" + bulletLineCount + " bullet lines)"
                    : "Content is broken into short, itemized lines";
            return new AtsCheckResult(
                    "Clear Structure Found",
                    detail + ", which is easier for both ATS systems and recruiters to scan.",
                    null,
                    AtsSeverity.PASSED);
        }

        return new AtsCheckResult(
                "Limited Visual Structure",
                "No bullet points were found, and content doesn't appear to be broken into short, itemized "
                        + "lines - it may read as one undifferentiated block of text.",
                "Use short bullet points for Experience and Projects entries (one accomplishment per line) "
                        + "instead of dense paragraphs.",
                AtsSeverity.MEDIUM);
    }
}
