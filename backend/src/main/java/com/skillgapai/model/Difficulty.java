package com.skillgapai.model;

/**
 * How hard a skill is generally considered to pick up.
 *
 * We use this later to rank MISSING skills: skills that are easier to
 * close (learn quickly) should be shown to the user before skills that
 * take months to become competent in.
 */
public enum Difficulty {
    EASY,
    MEDIUM,
    HARD
}
