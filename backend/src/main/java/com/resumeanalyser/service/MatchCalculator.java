package com.resumeanalyser.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/** Match Score = (Matched Skills / Total Required Skills) x 100 */
public final class MatchCalculator {

    public record Outcome(List<String> matched, List<String> missing, double score) { }

    private MatchCalculator() { }

    public static Outcome compute(List<String> required, Collection<String> candidateSkills) {
        Set<String> have = new HashSet<>();
        for (String s : candidateSkills) have.add(s.trim().toLowerCase(Locale.ROOT));

        List<String> matched = new ArrayList<>();
        List<String> missing = new ArrayList<>();
        for (String req : required) {
            if (have.contains(req.trim().toLowerCase(Locale.ROOT))) matched.add(req);
            else missing.add(req);
        }
        double score = required.isEmpty() ? 0.0 : matched.size() * 100.0 / required.size();
        return new Outcome(matched, missing, Math.round(score * 10.0) / 10.0);
    }
}
