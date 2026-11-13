package com.resumeanalyser.ui;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;

/** Small view-model records parsed from the backend JSON. */
public final class Models {

    private Models() { }

    public record JobItem(long id, String name, String skills) {
        @Override public String toString() { return name; }
    }

    public record ResumeItem(long id, String name, String email, String skills, double years, String education, String file) {
        @Override public String toString() { return name + "  (" + file + ")"; }
    }

    public static List<JobItem> jobs(JsonNode array) {
        List<JobItem> out = new ArrayList<>();
        for (JsonNode n : array) {
            out.add(new JobItem(n.path("jobId").asLong(), n.path("roleName").asText(), join(n.path("requiredSkills"), ", ")));
        }
        return out;
    }

    public static List<ResumeItem> resumes(JsonNode array) {
        List<ResumeItem> out = new ArrayList<>();
        for (JsonNode n : array) {
            out.add(new ResumeItem(n.path("resumeId").asLong(), n.path("candidateName").asText(), n.path("email").asText(""),
                    join(n.path("skills"), ", "), n.path("experienceYears").asDouble(), n.path("education").asText(""),
                    n.path("uploadedFileName").asText("")));
        }
        return out;
    }

    public static String join(JsonNode array, String separator) {
        List<String> parts = new ArrayList<>();
        if (array != null && array.isArray()) {
            for (JsonNode n : array) parts.add(n.asText());
        }
        return String.join(separator, parts);
    }
}
