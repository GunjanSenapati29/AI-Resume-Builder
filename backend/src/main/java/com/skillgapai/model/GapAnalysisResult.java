package com.skillgapai.model;

import java.util.List;

/**
 * The full output of comparing one resume against one job description.
 * This is the Java-side shape of what will eventually become the
 * "Gap Report" screen in the UI.
 */
public class GapAnalysisResult {

    private final List<MatchResult> matched;
    private final List<MatchResult> missing;
    private final List<MatchResult> underemphasized;
    private final double matchPercentage;

    public GapAnalysisResult(List<MatchResult> matched,
                              List<MatchResult> missing,
                              List<MatchResult> underemphasized,
                              double matchPercentage) {
        this.matched = matched;
        this.missing = missing;
        this.underemphasized = underemphasized;
        this.matchPercentage = matchPercentage;
    }

    public List<MatchResult> getMatched() {
        return matched;
    }

    public List<MatchResult> getMissing() {
        return missing;
    }

    public List<MatchResult> getUnderemphasized() {
        return underemphasized;
    }

    public double getMatchPercentage() {
        return matchPercentage;
    }
}
