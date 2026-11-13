package com.resumeanalyser.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/** Helpers for the comma separated skill columns. */
public final class CsvUtil {

    private CsvUtil() { }

    public static List<String> split(String csv) {
        List<String> out = new ArrayList<>();
        if (csv == null) return out;
        for (String part : csv.split(",")) {
            String t = part.trim();
            if (!t.isEmpty()) out.add(t);
        }
        return out;
    }

    public static String join(Collection<String> items) {
        return String.join(", ", items);
    }
}
