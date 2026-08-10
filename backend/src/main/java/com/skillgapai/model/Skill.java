package com.skillgapai.model;

import java.util.List;

/**
 * One entry in our skills taxonomy.
 *
 * A "canonical name" is the one official spelling we standardize on
 * (e.g. "JavaScript"). Real resumes and job descriptions use many
 * different spellings/abbreviations for the same skill (e.g. "JS",
 * "Javascript", "java script") — those alternate spellings are the
 * "synonyms" list.
 */
public class Skill {

    private final String canonicalName;
    private final List<String> synonyms;
    private final Difficulty difficulty;

    public Skill(String canonicalName, List<String> synonyms, Difficulty difficulty) {
        this.canonicalName = canonicalName;
        this.synonyms = synonyms;
        this.difficulty = difficulty;
    }

    public String getCanonicalName() {
        return canonicalName;
    }

    public List<String> getSynonyms() {
        return synonyms;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    /**
     * Every name this skill could reasonably appear under: its canonical
     * name plus all of its synonyms. Used when we scan resume text.
     */
    public List<String> allNames() {
        List<String> names = new java.util.ArrayList<>();
        names.add(canonicalName);
        names.addAll(synonyms);
        return names;
    }

    @Override
    public String toString() {
        return canonicalName;
    }
}
