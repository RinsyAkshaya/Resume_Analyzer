package com.resumeanalyser.service;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ResumeLogicTest {

    private final ResumeParserService parser = new ResumeParserService();

    @Test
    void formulaFromPptExample() {
        var outcome = MatchCalculator.compute(
                List.of("Java", "Spring Boot", "SQL", "Hibernate", "REST API"),
                List.of("Java", "Spring Boot", "SQL", "HTML"));
        assertEquals(List.of("Java", "Spring Boot", "SQL"), outcome.matched());
        assertEquals(List.of("Hibernate", "REST API"), outcome.missing());
        assertEquals(60.0, outcome.score());
    }

    @Test
    void parsesTextResume() {
        String text = "Candidate A\nEmail: a@x.com\nPhone: +91 9876543210\n\nSkills\nJava, Spring Boot, SQL, HTML\n\n"
                + "Experience\n2 years of experience\n\nEducation\nB.Tech CSE\n";
        var p = parser.parseText("a.txt", text);
        assertEquals("Candidate A", p.candidateName());
        assertEquals("a@x.com", p.email());
        assertEquals("+91 9876543210", p.phone());
        assertTrue(p.skills().containsAll(List.of("Java", "Spring Boot", "SQL", "HTML")));
        assertEquals(2.0, p.experienceYears());
        assertEquals("B.Tech CSE", p.education());
    }

    @Test
    void javaDoesNotMatchJavaScriptAndMySqlImpliesSql() {
        var p = parser.parseText("x.txt", "Jane Doe\nSkills: JavaScript, MySQL\n");
        assertFalse(p.skills().contains("Java"));
        assertTrue(p.skills().contains("JavaScript"));
        assertTrue(p.skills().contains("MySQL"));
        assertTrue(p.skills().contains("SQL"));
    }

    @Test
    void experienceFromDateRanges() {
        var p = parser.parseText("x.txt", "John Smith\n\nExperience\nDeveloper, Acme\n2019 - 2022\n\nEducation\nMBA\n");
        assertEquals(3.0, p.experienceYears());
    }
}
