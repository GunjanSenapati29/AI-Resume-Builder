package com.skillgapai.model;

/**
 * A skill that a job description asks for.
 *
 * In the full app, a later phase will extract this list automatically
 * from pasted JD text. For Phase 1, we build this list by hand to prove
 * the matching logic itself is correct before we build the extraction
 * step on top of it.
 *
 * "core" marks whether the JD treats this as a must-have requirement
 * (vs. a "nice to have" / bonus skill). We use this later to decide
 * whether a weakly-mentioned skill counts as "underemphasized".
 */
public class RequiredSkill {

    private final String skillName;
    private final boolean core;

    public RequiredSkill(String skillName, boolean core) {
        this.skillName = skillName;
        this.core = core;
    }

    public String getSkillName() {
        return skillName;
    }

    public boolean isCore() {
        return core;
    }
}
