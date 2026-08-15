package com.skillgapai.model;

import java.util.List;

/**
 * The full output of running every ATS compatibility check against one
 * resume's extracted text. Mirrors GapAnalysisResult's role for skill
 * matching - this is the Java-side shape of the "ATS Compatibility"
 * section of the Gap Report.
 */
public class AtsAnalysisResult {

    private final int score;
    private final List<AtsCheckResult> checks;

    public AtsAnalysisResult(int score, List<AtsCheckResult> checks) {
        this.score = score;
        this.checks = checks;
    }

    public int getScore() {
        return score;
    }

    public List<AtsCheckResult> getChecks() {
        return checks;
    }
}
