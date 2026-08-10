package com.skillgapai;

import com.skillgapai.matching.SkillMatchingService;
import com.skillgapai.model.GapAnalysisResult;
import com.skillgapai.model.MatchResult;
import com.skillgapai.model.RequiredSkill;
import com.skillgapai.taxonomy.SkillsTaxonomy;

import java.util.List;

/**
 * Phase 1 proof-of-concept: run the matching logic against ONE hardcoded
 * sample resume and ONE hardcoded list of JD-required skills, and print
 * a readable report to the console.
 *
 * No Spring, no database, no PDF parsing yet - this class exists purely
 * to prove SkillMatchingService itself is correct before we build
 * anything on top of it.
 *
 * Run with: mvn exec:java
 */
public class Phase1Demo {

    public static void main(String[] args) {
        // --- 1. Hardcoded sample resume text (as if already extracted from a PDF) ---
        String resumeText = """
                Gunjan is a final-year B.Tech Computer Science student with hands-on
                experience in Java and a strong interest in writing clean, readable Java
                code. During a college project, Gunjan built a Spring Boot backend that
                connects to a MySQL database to store student records. For the frontend,
                Gunjan is comfortable with JS for basic interactivity and has also used JS
                in a hackathon project to build a simple dashboard. Gunjan has used Git for
                version control across several personal projects and implemented a REST API
                for a hackathon submission. On a recent internal tool, Gunjan experimented
                with Doker to containerize a small demo application.
                """;

        // --- 2. Hardcoded required skills (as if already extracted from a JD) ---
        List<RequiredSkill> requiredSkills = List.of(
                new RequiredSkill("Java", true),
                new RequiredSkill("Spring Boot", true),
                new RequiredSkill("Spring Security", true),
                new RequiredSkill("REST APIs", true),
                new RequiredSkill("MySQL", true),
                new RequiredSkill("JavaScript", true),
                new RequiredSkill("React", true),
                new RequiredSkill("Git", false),
                new RequiredSkill("Docker", false),
                new RequiredSkill("JUnit", false)
        );

        // --- 3. Run the matcher ---
        SkillsTaxonomy taxonomy = SkillsTaxonomy.sampleTaxonomy();
        SkillMatchingService matcher = new SkillMatchingService(taxonomy);
        GapAnalysisResult result = matcher.analyze(resumeText, requiredSkills);

        // --- 4. Print a readable report ---
        printSection("MATCHED SKILLS (" + result.getMatched().size() + ")", result.getMatched());
        printSection("MISSING SKILLS - ranked easiest to close first (" + result.getMissing().size() + ")", result.getMissing());
        printSection("UNDEREMPHASIZED SKILLS (" + result.getUnderemphasized().size() + ")", result.getUnderemphasized());

        System.out.println();
        System.out.printf("OVERALL MATCH: %.1f%% (%d / %d required skills found)%n",
                result.getMatchPercentage(), result.getMatched().size(), requiredSkills.size());
    }

    private static void printSection(String title, List<MatchResult> results) {
        System.out.println();
        System.out.println("== " + title + " ==");
        if (results.isEmpty()) {
            System.out.println("  (none)");
            return;
        }
        for (MatchResult r : results) {
            System.out.println("  - " + r);
            System.out.println("      reason: " + r.getReason());
        }
    }
}
