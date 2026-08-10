package com.skillgapai.matching;

/**
 * Jaro-Winkler string similarity, implemented by hand in plain Java.
 *
 * NOTE ON WHY THIS IS HAND-WRITTEN INSTEAD OF USING APACHE COMMONS TEXT:
 * The spec (Section 4) calls for Apache Commons Text for this. This cloud
 * sandbox's network policy blocks Maven Central entirely (confirmed: even
 * Maven's own default plugins fail to download here), so no Maven
 * dependency can be fetched in this environment. Rather than block Phase 1
 * on that, we hand-roll the well-known Jaro-Winkler algorithm here. It is
 * a drop-in replacement — when this project is built somewhere with normal
 * internet access, this class can be deleted and calls can switch to
 * org.apache.commons.text.similarity.JaroWinklerSimilarity with no change
 * to SkillMatchingService's logic or behaviour.
 *
 * Returns a score between 0.0 (completely different) and 1.0 (identical).
 */
public final class StringSimilarity {

    /** How much extra weight to give a shared prefix (standard Jaro-Winkler default). */
    private static final double PREFIX_SCALING_FACTOR = 0.1;

    /** Jaro-Winkler only boosts a shared prefix up to this many characters. */
    private static final int MAX_PREFIX_LENGTH = 4;

    private StringSimilarity() {
        // utility class - no instances
    }

    public static double jaroWinkler(String s1, String s2) {
        double jaro = jaroSimilarity(s1, s2);

        int prefixLength = commonPrefixLength(s1, s2);
        return jaro + (prefixLength * PREFIX_SCALING_FACTOR * (1.0 - jaro));
    }

    private static double jaroSimilarity(String s1, String s2) {
        if (s1.equals(s2)) {
            return 1.0;
        }

        int len1 = s1.length();
        int len2 = s2.length();
        if (len1 == 0 || len2 == 0) {
            return 0.0;
        }

        // Two characters are considered "matching" if they are the same
        // and not too far apart in the two strings.
        int matchDistance = Math.max(0, Math.max(len1, len2) / 2 - 1);

        boolean[] s1Matches = new boolean[len1];
        boolean[] s2Matches = new boolean[len2];

        int matches = 0;
        for (int i = 0; i < len1; i++) {
            int start = Math.max(0, i - matchDistance);
            int end = Math.min(i + matchDistance + 1, len2);

            for (int j = start; j < end; j++) {
                if (s2Matches[j] || s1.charAt(i) != s2.charAt(j)) {
                    continue;
                }
                s1Matches[i] = true;
                s2Matches[j] = true;
                matches++;
                break;
            }
        }

        if (matches == 0) {
            return 0.0;
        }

        // Transpositions: matched characters that appear in a different order.
        int transpositions = 0;
        int k = 0;
        for (int i = 0; i < len1; i++) {
            if (!s1Matches[i]) {
                continue;
            }
            while (!s2Matches[k]) {
                k++;
            }
            if (s1.charAt(i) != s2.charAt(k)) {
                transpositions++;
            }
            k++;
        }
        transpositions /= 2;

        double m = matches;
        return (m / len1 + m / len2 + (m - transpositions) / m) / 3.0;
    }

    private static int commonPrefixLength(String s1, String s2) {
        int maxLength = Math.min(MAX_PREFIX_LENGTH, Math.min(s1.length(), s2.length()));
        int i = 0;
        while (i < maxLength && s1.charAt(i) == s2.charAt(i)) {
            i++;
        }
        return i;
    }
}
