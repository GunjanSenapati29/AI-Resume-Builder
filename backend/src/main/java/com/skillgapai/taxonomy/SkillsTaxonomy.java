package com.skillgapai.taxonomy;

import com.skillgapai.model.Difficulty;
import com.skillgapai.model.Skill;

import java.util.ArrayList;
import java.util.List;

/**
 * Our "dictionary" of known skills: canonical name + synonyms + difficulty.
 *
 * In Phase 2 this will move into the SkillsTaxonomy MySQL table (Section 7
 * of the spec) and be loaded from the database instead of hardcoded here.
 * For Phase 1 we hardcode a small, realistic set so we can prove the
 * matching logic works before adding a database.
 */
public class SkillsTaxonomy {

    private final List<Skill> skills;

    public SkillsTaxonomy(List<Skill> skills) {
        this.skills = skills;
    }

    /**
     * A small taxonomy covering the kind of skills that would show up
     * in a Java Full Stack job description. Good enough to exercise
     * every branch of the matcher (exact, synonym, fuzzy, missing).
     */
    public static SkillsTaxonomy sampleTaxonomy() {
        List<Skill> skills = new ArrayList<>();

        skills.add(new Skill("Java", List.of("core java", "java se"), Difficulty.MEDIUM));
        skills.add(new Skill("Spring Boot", List.of("springboot", "spring-boot"), Difficulty.MEDIUM));
        skills.add(new Skill("Spring Security", List.of("spring-security"), Difficulty.HARD));
        // Note: deliberately NOT including bare "rest" as a synonym - it's a
        // common English word on its own and would cause false matches on
        // unrelated resume text (e.g. "took a rest between projects").
        skills.add(new Skill("REST APIs", List.of("rest api", "restful api", "restful apis"), Difficulty.EASY));
        skills.add(new Skill("MySQL", List.of("my sql", "mysql db"), Difficulty.EASY));
        skills.add(new Skill("JavaScript", List.of("js", "java script"), Difficulty.MEDIUM));
        skills.add(new Skill("React", List.of("react.js", "reactjs"), Difficulty.MEDIUM));
        skills.add(new Skill("Git", List.of("github", "version control"), Difficulty.EASY));
        skills.add(new Skill("Docker", List.of("containerization"), Difficulty.HARD));
        skills.add(new Skill("JUnit", List.of("unit testing"), Difficulty.EASY));

        return new SkillsTaxonomy(skills);
    }

    public List<Skill> getSkills() {
        return skills;
    }

    /**
     * Look up a Skill by any of its names (canonical or synonym),
     * case-insensitively. Returns null if we don't recognize the name
     * (kept simple/explicit for Phase 1 instead of Optional, so the
     * control flow is easy to trace in a viva).
     */
    public Skill findSkillByName(String name) {
        String normalized = name.trim().toLowerCase();

        for (Skill skill : skills) {
            for (String candidateName : skill.allNames()) {
                if (candidateName.toLowerCase().equals(normalized)) {
                    return skill;
                }
            }
        }
        return null;
    }
}
