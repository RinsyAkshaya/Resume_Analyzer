package com.resumeanalyser.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Dictionary of recognised skills. Skill detection is real text matching: every skill has one or more
 * aliases that are searched (case-insensitive, whole-word) in the extracted resume text.
 * "MySQL", "PostgreSQL", "Oracle", "SQLite" and "SQL Server" also imply the SQL skill.
 */
public final class SkillCatalog {

    public static final String LANGUAGES = "Programming Languages";
    public static final String FRAMEWORKS = "Frameworks & Libraries";
    public static final String WEB = "Web Technologies";
    public static final String DATABASES = "Databases";
    public static final String DATA = "Data & Analytics";
    public static final String TOOLS = "Tools & Concepts";
    public static final String SOFT = "Soft Skills";

    private record Skill(String name, String category, List<Pattern> patterns, List<String> implies) { }

    private static final List<Skill> SKILLS = new ArrayList<>();
    private static final Map<String, String> CATEGORY_BY_NAME = new HashMap<>();

    static {
        // Programming languages
        add("Java", LANGUAGES, "java");
        add("Python", LANGUAGES, "python");
        add("JavaScript", LANGUAGES, "javascript", "ecmascript");
        add("TypeScript", LANGUAGES, "typescript");
        add("C++", LANGUAGES, "c++");
        add("C#", LANGUAGES, "c#");
        add("PHP", LANGUAGES, "php");
        add("Kotlin", LANGUAGES, "kotlin");
        add("SQL", LANGUAGES, "sql", "pl/sql", "t-sql");

        // Frameworks & libraries
        add("Spring Boot", FRAMEWORKS, "spring boot", "springboot");
        add("Spring", FRAMEWORKS, "spring framework", "spring mvc", "spring security");
        add("Hibernate", FRAMEWORKS, "hibernate");
        add("Django", FRAMEWORKS, "django");
        add("Flask", FRAMEWORKS, "flask");
        add("FastAPI", FRAMEWORKS, "fastapi");
        add("React", FRAMEWORKS, "react", "reactjs", "react.js");
        add("Angular", FRAMEWORKS, "angular", "angularjs");
        add("Vue", FRAMEWORKS, "vue", "vue.js", "vuejs");
        add("Node.js", FRAMEWORKS, "node.js", "nodejs");
        add("Express", FRAMEWORKS, "express.js", "expressjs");
        add("Bootstrap", FRAMEWORKS, "bootstrap");
        add("JUnit", FRAMEWORKS, "junit");

        // Web technologies
        add("HTML", WEB, "html", "html5");
        add("CSS", WEB, "css", "css3");
        add("REST API", WEB, "rest api", "rest apis", "restful", "restful api", "restful apis", "restful services", "rest services", "rest web services");
        add("Microservices", WEB, "microservices", "micro services");

        // Databases (the relational ones also imply SQL)
        addImplying("MySQL", DATABASES, List.of("SQL"), "mysql");
        addImplying("PostgreSQL", DATABASES, List.of("SQL"), "postgresql", "postgres");
        addImplying("Oracle", DATABASES, List.of("SQL"), "oracle db", "oracle database");
        addImplying("SQL Server", DATABASES, List.of("SQL"), "sql server", "mssql");
        addImplying("SQLite", DATABASES, List.of("SQL"), "sqlite");
        add("MongoDB", DATABASES, "mongodb", "mongo db");

        // Data & analytics
        add("Excel", DATA, "excel", "ms excel", "microsoft excel");
        add("Power BI", DATA, "power bi", "powerbi");
        add("Tableau", DATA, "tableau");
        add("Pandas", DATA, "pandas");
        add("NumPy", DATA, "numpy");
        add("Statistics", DATA, "statistics", "statistical analysis");
        add("Machine Learning", DATA, "machine learning", "ml");
        add("Scikit-learn", DATA, "scikit-learn", "scikit learn", "sklearn");
        add("TensorFlow", DATA, "tensorflow");
        add("Data Visualization", DATA, "data visualization", "data visualisation");

        // Tools & CS concepts
        add("Data Structures", TOOLS, "data structures", "data structure", "dsa");
        add("Algorithms", TOOLS, "algorithms", "algorithm", "dsa");
        add("OOP", TOOLS, "oop", "oops", "object oriented", "object-oriented");
        add("Git", TOOLS, "git");
        addImplying("GitHub", TOOLS, List.of("Git"), "github");
        add("Docker", TOOLS, "docker");
        add("Maven", TOOLS, "maven");
        add("Linux", TOOLS, "linux");
        add("AWS", TOOLS, "aws", "amazon web services");
        add("Agile", TOOLS, "agile", "scrum");

        // Soft skills (kept in "skills", excluded from "technical skills")
        add("Communication", SOFT, "communication", "communication skills");
        add("Leadership", SOFT, "leadership");
        add("Teamwork", SOFT, "teamwork", "team work", "team player");
        add("Problem Solving", SOFT, "problem solving", "problem-solving");
        add("Time Management", SOFT, "time management");
    }

    private SkillCatalog() { }

    private static void add(String name, String category, String... aliases) {
        addImplying(name, category, List.of(), aliases);
    }

    private static void addImplying(String name, String category, List<String> implies, String... aliases) {
        List<Pattern> patterns = new ArrayList<>();
        for (String alias : aliases) patterns.add(aliasPattern(alias));
        SKILLS.add(new Skill(name, category, patterns, implies));
        CATEGORY_BY_NAME.put(name.toLowerCase(), category);
    }

    /** Whole-word, case-insensitive pattern. Spaces in an alias match optional space/hyphen. */
    private static Pattern aliasPattern(String alias) {
        String[] parts = alias.trim().split("\\s+");
        StringBuilder sb = new StringBuilder("(?<![A-Za-z0-9+#])");
        for (int i = 0; i < parts.length; i++) {
            if (i > 0) sb.append("[\\s\\-]*");
            sb.append(Pattern.quote(parts[i]));
        }
        sb.append("(?![A-Za-z0-9+#])");
        return Pattern.compile(sb.toString(), Pattern.CASE_INSENSITIVE);
    }

    /** Returns canonical skill names found in the text, in catalog order. */
    public static List<String> extract(String text) {
        Set<String> found = new LinkedHashSet<>();
        for (Skill s : SKILLS) {
            for (Pattern p : s.patterns()) {
                if (p.matcher(text).find()) {
                    found.add(s.name());
                    found.addAll(s.implies());
                    break;
                }
            }
        }
        List<String> ordered = new ArrayList<>();
        for (Skill s : SKILLS) {
            if (found.contains(s.name())) ordered.add(s.name());
        }
        return ordered;
    }

    public static String categoryOf(String skill) {
        return CATEGORY_BY_NAME.get(skill.toLowerCase());
    }

    /** Technical skills grouped by category (soft skills and unknown skills are excluded). */
    public static Map<String, List<String>> technicalByCategory(Collection<String> skills) {
        Map<String, List<String>> grouped = new LinkedHashMap<>();
        for (String category : new String[]{LANGUAGES, FRAMEWORKS, WEB, DATABASES, DATA, TOOLS}) {
            List<String> inCategory = new ArrayList<>();
            for (String skill : skills) {
                if (category.equals(categoryOf(skill))) inCategory.add(skill);
            }
            if (!inCategory.isEmpty()) grouped.put(category, inCategory);
        }
        return grouped;
    }
}
