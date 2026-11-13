package com.resumeanalyser.service;

import com.resumeanalyser.exception.ApiException;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Extracts text from PDF / DOCX / TXT (PDFBox + Apache POI) and pulls out structured candidate data. */
@Service
public class ResumeParserService {

    public record ParsedResume(String candidateName, String email, String phone, List<String> skills,
                               double experienceYears, String education) { }

    // ------------------------------------------------------------------ text extraction

    public String extractText(String fileName, byte[] data) {
        String ext = extension(fileName);
        try {
            String text = switch (ext) {
                case "pdf" -> pdfText(data);
                case "docx" -> docxText(data);
                case "txt" -> new String(data, StandardCharsets.UTF_8);
                default -> throw new ApiException(HttpStatus.BAD_REQUEST,
                        "Unsupported file type. Please upload a PDF, DOCX or TXT file.");
            };
            if (text == null || text.isBlank()) {
                throw new ApiException(HttpStatus.UNPROCESSABLE_ENTITY,
                        "No readable text found in the file (scanned/image-only resumes are not supported).");
            }
            return text;
        } catch (IOException e) {
            throw new ApiException(HttpStatus.UNPROCESSABLE_ENTITY, "Could not read the file: " + e.getMessage());
        }
    }

    private String pdfText(byte[] data) throws IOException {
        try (PDDocument doc = Loader.loadPDF(data)) {
            return new PDFTextStripper().getText(doc);
        }
    }

    private String docxText(byte[] data) throws IOException {
        try (XWPFDocument doc = new XWPFDocument(new ByteArrayInputStream(data));
             XWPFWordExtractor extractor = new XWPFWordExtractor(doc)) {
            return extractor.getText();
        }
    }

    public static String extension(String fileName) {
        if (fileName == null) return "";
        int dot = fileName.lastIndexOf('.');
        return dot < 0 ? "" : fileName.substring(dot + 1).toLowerCase(Locale.ROOT);
    }

    // ------------------------------------------------------------------ parsing

    public ParsedResume parseText(String fileName, String rawText) {
        String text = rawText.replace('\u00A0', ' ').replace("\r", "");
        return new ParsedResume(
                extractName(text, fileName),
                extractEmail(text),
                extractPhone(text),
                new ArrayList<>(SkillCatalog.extract(text)),
                extractExperience(text),
                extractEducation(text));
    }

    // ---- email / phone

    private static final Pattern EMAIL = Pattern.compile("[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}");
    private static final Pattern PHONE_IN = Pattern.compile("(?<![\\d])(?:\\+91[\\s-]?)?[6-9]\\d{4}[\\s-]?\\d{5}(?!\\d)");
    private static final Pattern PHONE_INTL = Pattern.compile("(?<![\\d])\\+?\\d{1,3}[\\s.-]?\\(?\\d{3}\\)?[\\s.-]?\\d{3}[\\s.-]?\\d{4}(?!\\d)");

    private String extractEmail(String text) {
        Matcher m = EMAIL.matcher(text);
        return m.find() ? m.group().toLowerCase(Locale.ROOT) : null;
    }

    private String extractPhone(String text) {
        Matcher m = PHONE_IN.matcher(text);
        if (m.find()) return m.group().trim();
        m = PHONE_INTL.matcher(text);
        return m.find() ? m.group().trim() : null;
    }

    // ---- name

    private static final Pattern NAME_LABEL = Pattern.compile("(?im)^\\s*(?:full\\s+)?name\\s*[:\\-]\\s*([A-Za-z][A-Za-z.'\\- ]{1,60})\\s*$");
    private static final Pattern NAME_LINE = Pattern.compile("^[A-Za-z][A-Za-z.'\\-]*(?:\\s+[A-Za-z][A-Za-z.'\\-]*){1,3}$");
    private static final Set<String> NON_NAME_WORDS = Set.of("resume", "curriculum", "vitae", "cv", "profile", "summary",
            "objective", "contact", "education", "skills", "experience", "email", "phone", "address", "projects",
            "declaration", "career", "details", "personal", "information", "page", "technical");

    private String extractName(String text, String fileName) {
        Matcher label = NAME_LABEL.matcher(text);
        if (label.find()) return titleCase(label.group(1).trim());

        String[] lines = text.split("\n");
        int checked = 0;
        for (String raw : lines) {
            String line = raw.trim().replaceAll("\\s+", " ");
            if (line.isEmpty()) continue;
            if (++checked > 10) break;
            if (line.length() > 50 || !NAME_LINE.matcher(line).matches()) continue;
            boolean bad = false;
            for (String w : line.toLowerCase(Locale.ROOT).split(" ")) {
                if (NON_NAME_WORDS.contains(w)) { bad = true; break; }
            }
            if (!bad) return titleCase(line);
        }

        String base = fileName == null ? "" : fileName.replaceAll("\\.[^.]*$", "");
        base = base.replaceAll("(?i)resume|cv", " ").replaceAll("[_\\-.]+", " ").trim();
        return base.isEmpty() ? "Unknown Candidate" : titleCase(base);
    }

    private String titleCase(String s) {
        StringBuilder sb = new StringBuilder();
        for (String w : s.trim().split("\\s+")) {
            if (w.isEmpty()) continue;
            if (sb.length() > 0) sb.append(' ');
            boolean allUpper = w.equals(w.toUpperCase(Locale.ROOT)) && w.length() > 2;
            sb.append(Character.toUpperCase(w.charAt(0)));
            sb.append(allUpper ? w.substring(1).toLowerCase(Locale.ROOT) : w.substring(1));
        }
        return sb.toString();
    }

    // ---- sections

    private static final Pattern ANY_HEADING = Pattern.compile(
            "(?im)^\\s*(?:work\\s+experience|professional\\s+experience|experience|employment(?:\\s+history)?|"
                    + "education(?:al\\s+qualifications?)?|academic(?:\\s+qualifications?)?|(?:technical\\s+)?skills|"
                    + "projects?|certifications?|achievements?|summary|objective|profile|interests|hobbies|declaration)\\s*:?\\s*$");
    private static final Pattern EXPERIENCE_HEADING = Pattern.compile(
            "(?im)^\\s*(?:work\\s+experience|professional\\s+experience|experience|employment(?:\\s+history)?)\\s*:?\\s*$");
    private static final Pattern EDUCATION_HEADING = Pattern.compile(
            "(?im)^\\s*(?:education(?:al\\s+qualifications?)?|academic(?:\\s+qualifications?)?)\\s*:?\\s*$");

    private String section(String text, Pattern heading) {
        Matcher h = heading.matcher(text);
        if (!h.find()) return "";
        int start = h.end();
        int end = text.length();
        Matcher any = ANY_HEADING.matcher(text);
        while (any.find()) {
            if (any.start() >= start) { end = any.start(); break; }
        }
        return text.substring(start, end);
    }

    // ---- experience

    private static final Pattern EXP_A = Pattern.compile(
            "(?i)(\\d{1,2}(?:\\.\\d+)?)\\+?\\s*(?:years?|yrs?)(?:\\s+of)?\\s+(?:(?:work|professional|industry|relevant|total|hands-on)\\s+)*experience");
    private static final Pattern EXP_B = Pattern.compile(
            "(?i)experience\\s*[:\\-\\u2013]?\\s*(?:of\\s+)?(\\d{1,2}(?:\\.\\d+)?)\\+?\\s*(?:years?|yrs?)");
    private static final Pattern FRESHER = Pattern.compile("(?i)\\bfresher\\b|\\bno\\s+(?:prior\\s+|work\\s+)?experience\\b");
    private static final String MONTH = "(jan|feb|mar|apr|may|jun|jul|aug|sep|oct|nov|dec)";
    private static final Pattern RANGE = Pattern.compile(
            "(?i)(?:" + MONTH + "[a-z]*\\.?,?\\s+)?((?:19|20)\\d{2})\\s*(?:-|\\u2013|\\u2014|to)\\s*"
                    + "(?:(?:" + MONTH + "[a-z]*\\.?,?\\s+)?((?:19|20)\\d{2})|(present|current|now|till\\s+date))");

    private double extractExperience(String text) {
        boolean found = false;
        double best = 0;
        for (Pattern p : List.of(EXP_A, EXP_B)) {
            Matcher m = p.matcher(text);
            while (m.find()) {
                double v = Double.parseDouble(m.group(1));
                if (v <= 50) { best = Math.max(best, v); found = true; }
            }
        }
        if (found) return round1(best);
        if (FRESHER.matcher(text).find()) return 0.0;
        return round1(sumDateRanges(section(text, EXPERIENCE_HEADING)));
    }

    private double sumDateRanges(String experienceSection) {
        double total = 0;
        Matcher m = RANGE.matcher(experienceSection);
        LocalDate now = LocalDate.now();
        while (m.find()) {
            double start = toYear(m.group(1), m.group(2));
            double end = m.group(5) != null
                    ? now.getYear() + (now.getMonthValue() - 1) / 12.0
                    : toYear(m.group(3), m.group(4));
            double diff = end - start;
            if (diff > 0 && diff <= 40) total += diff;
        }
        return total;
    }

    private double toYear(String month, String year) {
        double y = Double.parseDouble(year);
        if (month == null) return y;
        int idx = "janfebmaraprmayjunjulaugsepoctnovdec".indexOf(month.toLowerCase(Locale.ROOT).substring(0, 3)) / 3;
        return y + Math.max(idx, 0) / 12.0;
    }

    private double round1(double v) {
        return Math.round(v * 10.0) / 10.0;
    }

    // ---- education

    private static final Pattern DEGREE_ABBR = Pattern.compile(
            "(?<![A-Za-z])(?:B\\.?\\s?Tech|M\\.?\\s?Tech|B\\.E\\.?|M\\.E\\.?|B\\.?\\s?Sc|M\\.?\\s?Sc|BCA|MCA|B\\.?\\s?Com|M\\.?\\s?Com|MBA|BBA|Ph\\.?D)(?![A-Za-z])");
    private static final Pattern DEGREE_WORD = Pattern.compile("(?i)\\b(?:bachelor|master|diploma|doctorate)\\b");

    private String extractEducation(String text) {
        String scope = section(text, EDUCATION_HEADING);
        if (scope.isBlank()) scope = text;
        Set<String> lines = new LinkedHashSet<>();
        for (String raw : scope.split("\n")) {
            String line = raw.trim().replaceAll("\\s+", " ");
            if (line.isEmpty()) continue;
            if (DEGREE_ABBR.matcher(line).find() || DEGREE_WORD.matcher(line).find()) {
                lines.add(line.length() > 90 ? line.substring(0, 90) : line);
            }
            if (lines.size() >= 3) break;
        }
        return lines.isEmpty() ? "Not specified" : String.join("; ", lines);
    }
}
