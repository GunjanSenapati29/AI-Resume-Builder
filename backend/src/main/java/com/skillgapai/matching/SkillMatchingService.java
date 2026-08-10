package com.skillgapai.matching;

import com.skillgapai.model.Difficulty;
import com.skillgapai.model.GapAnalysisResult;
import com.skillgapai.model.MatchResult;
import com.skillgapai.model.MatchType;
import com.skillgapai.model.RequiredSkill;
import com.skillgapai.model.Skill;
import com.skillgapai.taxonomy.SkillsTaxonomy;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The core Phase 1 logic: compare a resume's plain text against a list of
 * skills a job description requires, and explain WHY each one is
 * matched, missing, or underemphasized.
 *
 * This class deliberately has NO Spring, NO database, and NO PDF parsing.
 * Those get layered on top of this class in later phases — this class is
 * the one piece of logic everything else depends on, so it has to be
 * correct and easy to reason about on its own first.
 */
public class SkillMatchingService {

    /**
     * How similar two words need to be (0.0 - 1.0) to count as a "close
     * spelling" fuzzy match. Started at 0.85 but Phase 1 testing (see
     * Phase1Demo output) showed that caught false positives - e.g.
     * "React" vs the unrelated word "recent" scored 0.86. Raised to 0.92
     * after measuring real scores for both genuine typos ("doker" vs
     * "docker" = 0.9556, kept) and false positives ("recent" vs "react" =
     * 0.8578, "spring boot" vs "spring security" = 0.8521, both excluded).
     */
    private static final double FUZZY_SIMILARITY_THRESHOLD = 0.92;

    private final SkillsTaxonomy taxonomy;

    /**
     * Every canonical/synonym name across the WHOLE taxonomy, lowercased.
     * Used as a guard in fuzzy matching: if a candidate word/phrase from
     * the resume is itself a real, different, known skill (e.g. "spring
     * boot" while we're checking "Spring Security"), it must NOT be
     * treated as a typo of the skill we're currently checking - it's a
     * mention of a different, real skill and should be left for that
     * skill's own exact/synonym check instead.
     */
    private final Set<String> allKnownSkillNames;

    public SkillMatchingService(SkillsTaxonomy taxonomy) {
        this.taxonomy = taxonomy;
        this.allKnownSkillNames = buildAllKnownNames(taxonomy);
    }

    private static Set<String> buildAllKnownNames(SkillsTaxonomy taxonomy) {
        Set<String> names = new HashSet<>();
        for (Skill skill : taxonomy.getSkills()) {
            for (String name : skill.allNames()) {
                names.add(name.toLowerCase());
            }
        }
        return names;
    }

    /**
     * Compares resumeText against every entry in requiredSkills and
     * returns the full gap analysis: matched, missing (ranked easiest
     * to close first), underemphasized, and an overall match percentage.
     */
    public GapAnalysisResult analyze(String resumeText, List<RequiredSkill> requiredSkills) {
        String normalizedResume = resumeText.toLowerCase();

        List<MatchResult> matched = new ArrayList<>();
        List<MatchResult> missing = new ArrayList<>();
        List<MatchResult> underemphasized = new ArrayList<>();

        for (RequiredSkill required : requiredSkills) {
            Skill knownSkill = taxonomy.findSkillByName(required.getSkillName());

            // If the JD mentions a skill our taxonomy doesn't know about yet,
            // we still try to match it literally instead of silently skipping it.
            Difficulty difficulty = (knownSkill != null) ? knownSkill.getDifficulty() : Difficulty.MEDIUM;
            List<String> namesToCheck = (knownSkill != null) ? knownSkill.allNames() : List.of(required.getSkillName());

            MatchAttempt attempt = findInResume(namesToCheck, normalizedResume);

            if (attempt.matchType == MatchType.NOT_FOUND) {
                missing.add(new MatchResult(
                        required.getSkillName(), MatchType.NOT_FOUND,
                        "Not found anywhere in the resume text.", 0, difficulty));
                continue;
            }

            MatchResult result = new MatchResult(
                    required.getSkillName(), attempt.matchType, attempt.reason,
                    attempt.occurrenceCount, difficulty);
            matched.add(result);

            // A core requirement mentioned only once is flagged as "underemphasized" -
            // it's technically present, but not shown with much weight on the resume.
            if (required.isCore() && attempt.occurrenceCount <= 1) {
                underemphasized.add(new MatchResult(
                        required.getSkillName(), attempt.matchType,
                        "Mentioned only once, even though the JD marks it as a core requirement.",
                        attempt.occurrenceCount, difficulty));
            }
        }

        // Rank missing skills easiest-to-close first (Difficulty enum order:
        // EASY < MEDIUM < HARD, so a plain compareTo does the job).
        missing.sort((a, b) -> {
            int byDifficulty = a.getDifficulty().compareTo(b.getDifficulty());
            if (byDifficulty != 0) {
                return byDifficulty;
            }
            return a.getSkillName().compareToIgnoreCase(b.getSkillName());
        });

        double matchPercentage = requiredSkills.isEmpty()
                ? 0.0
                : (matched.size() * 100.0) / requiredSkills.size();

        return new GapAnalysisResult(matched, missing, underemphasized, matchPercentage);
    }

    /**
     * Tries, in order: exact canonical name match -> synonym match ->
     * fuzzy/typo match. Stops at the first one that succeeds.
     */
    private MatchAttempt findInResume(List<String> namesToCheck, String normalizedResume) {
        for (int i = 0; i < namesToCheck.size(); i++) {
            String name = namesToCheck.get(i).toLowerCase();
            int count = countOccurrences(normalizedResume, name);

            if (count > 0) {
                boolean isCanonicalName = (i == 0);
                if (isCanonicalName) {
                    String reason = "Found exact term \"" + namesToCheck.get(i) + "\" in the resume.";
                    return new MatchAttempt(MatchType.EXACT, reason, count);
                } else {
                    String reason = "Found via synonym: \"" + namesToCheck.get(i)
                            + "\" matches \"" + namesToCheck.get(0) + "\".";
                    return new MatchAttempt(MatchType.SYNONYM, reason, count);
                }
            }
        }

        return tryFuzzyMatch(namesToCheck.get(0), normalizedResume);
    }

    /**
     * Slides a word-window of the same length as the target skill name
     * across the resume text and finds the closest-spelled candidate.
     * This is what lets us catch things like "Sprnig Boot" as a likely
     * typo for "Spring Boot" even though it's not in our synonym list.
     */
    private MatchAttempt tryFuzzyMatch(String canonicalName, String normalizedResume) {
        String[] resumeTokens = normalizedResume.split("[^a-z0-9+#.]+");
        String[] targetTokens = canonicalName.toLowerCase().split("\\s+");
        int windowSize = targetTokens.length;

        String bestCandidate = null;
        double bestSimilarity = 0.0;

        for (int start = 0; start + windowSize <= resumeTokens.length; start++) {
            StringBuilder windowBuilder = new StringBuilder();
            for (int offset = 0; offset < windowSize; offset++) {
                if (offset > 0) {
                    windowBuilder.append(" ");
                }
                windowBuilder.append(resumeTokens[start + offset]);
            }
            String candidate = windowBuilder.toString();
            if (candidate.isBlank()) {
                continue;
            }

            // Guard against cross-skill false positives: if this candidate
            // text is itself a real, known skill name (just a DIFFERENT
            // skill than the one we're checking), it's not a typo - skip it.
            if (allKnownSkillNames.contains(candidate)) {
                continue;
            }

            double similarity = StringSimilarity.jaroWinkler(canonicalName.toLowerCase(), candidate);
            if (similarity > bestSimilarity) {
                bestSimilarity = similarity;
                bestCandidate = candidate;
            }
        }

        if (bestCandidate != null && bestSimilarity >= FUZZY_SIMILARITY_THRESHOLD) {
            String reason = String.format(
                    "Found close spelling \"%s\" (%.0f%% similar to \"%s\") - possible typo.",
                    bestCandidate, bestSimilarity * 100, canonicalName);
            return new MatchAttempt(MatchType.FUZZY, reason, 1);
        }

        return new MatchAttempt(MatchType.NOT_FOUND, "", 0);
    }

    /**
     * Counts whole-word/whole-phrase occurrences of term inside text.
     * We use word boundaries (\b) so "Java" does not falsely match
     * inside "JavaScript".
     */
    private int countOccurrences(String text, String term) {
        if (term.isBlank()) {
            return 0;
        }
        Pattern pattern = Pattern.compile("\\b" + Pattern.quote(term) + "\\b");
        Matcher matcher = pattern.matcher(text);

        int count = 0;
        while (matcher.find()) {
            count++;
        }
        return count;
    }

    /** Small internal holder for an in-progress match attempt. */
    private static class MatchAttempt {
        final MatchType matchType;
        final String reason;
        final int occurrenceCount;

        MatchAttempt(MatchType matchType, String reason, int occurrenceCount) {
            this.matchType = matchType;
            this.reason = reason;
            this.occurrenceCount = occurrenceCount;
        }
    }
}
