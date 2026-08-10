package com.skillgapai.model;

/**
 * HOW a required skill was (or wasn't) found in the resume text.
 * This is the core of "visible reasoning" from the UI/UX brief — we never
 * just say "matched", we say matched *how*.
 */
public enum MatchType {
    /** The exact canonical skill name appears in the resume. */
    EXACT,

    /** A known synonym/abbreviation of the skill appears (e.g. "JS" for "JavaScript"). */
    SYNONYM,

    /** No exact/synonym hit, but a close-spelling match was found (typo tolerance). */
    FUZZY,

    /** The skill does not appear in the resume at all. */
    NOT_FOUND
}
