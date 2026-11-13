package com.resumeanalyser.controller;

import com.resumeanalyser.dto.AnalysisResponse;
import com.resumeanalyser.service.AnalysisService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class AnalysisController {

    private final AnalysisService analysis;

    public AnalysisController(AnalysisService analysis) {
        this.analysis = analysis;
    }

    /** POST /api/analyse/{resumeId}?jobId=1 */
    @PostMapping("/analyse/{resumeId}")
    public AnalysisResponse analyse(@PathVariable("resumeId") Long resumeId, @RequestParam("jobId") Long jobId) {
        return analysis.analyse(resumeId, jobId);
    }

    /** GET /api/results/{resumeId} - every stored analysis of one resume. */
    @GetMapping("/results/{resumeId}")
    public List<AnalysisResponse> results(@PathVariable("resumeId") Long resumeId) {
        return analysis.resultsForResume(resumeId);
    }

    /** GET /api/results/recent?limit=10 - latest analyses across all resumes (dashboard). */
    @GetMapping("/results/recent")
    public List<AnalysisResponse> recent(@RequestParam(value = "limit", defaultValue = "10") int limit) {
        return analysis.recent(limit);
    }
}
