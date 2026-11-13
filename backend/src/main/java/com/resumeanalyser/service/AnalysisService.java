package com.resumeanalyser.service;

import com.resumeanalyser.dto.AnalysisResponse;
import com.resumeanalyser.entity.AnalysisResult;
import com.resumeanalyser.entity.JobRole;
import com.resumeanalyser.entity.Resume;
import com.resumeanalyser.exception.ApiException;
import com.resumeanalyser.repository.AnalysisResultRepository;
import com.resumeanalyser.repository.JobRoleRepository;
import com.resumeanalyser.repository.ResumeRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AnalysisService {

    private final ResumeRepository resumes;
    private final JobRoleRepository jobs;
    private final AnalysisResultRepository results;
    private final double shortlistThreshold;

    public AnalysisService(ResumeRepository resumes, JobRoleRepository jobs, AnalysisResultRepository results,
                           @Value("${analysis.shortlist-threshold:50}") double shortlistThreshold) {
        this.resumes = resumes;
        this.jobs = jobs;
        this.results = results;
        this.shortlistThreshold = shortlistThreshold;
    }

    /** Matches the resume's extracted skills against the role's required skills and stores the result. */
    @Transactional
    public AnalysisResponse analyse(Long resumeId, Long jobId) {
        Resume resume = resumes.findById(resumeId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Resume not found."));
        JobRole job = jobs.findById(jobId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Job role not found."));

        MatchCalculator.Outcome outcome = MatchCalculator.compute(
                CsvUtil.split(job.getRequiredSkills()), CsvUtil.split(resume.getSkills()));

        // Re-analysing the same resume for the same role updates the stored result.
        AnalysisResult result = results.findByResume_ResumeIdAndJobRole_JobId(resumeId, jobId)
                .orElseGet(AnalysisResult::new);
        result.setResume(resume);
        result.setJobRole(job);
        result.setMatchScore(outcome.score());
        result.setMatchedSkills(CsvUtil.join(outcome.matched()));
        result.setMissingSkills(CsvUtil.join(outcome.missing()));
        result.setShortlisted(outcome.score() >= shortlistThreshold);
        result.setAnalyzedAt(LocalDateTime.now());
        return Mapper.toAnalysis(results.save(result));
    }

    @Transactional(readOnly = true)
    public List<AnalysisResponse> resultsForResume(Long resumeId) {
        if (!resumes.existsById(resumeId)) throw new ApiException(HttpStatus.NOT_FOUND, "Resume not found.");
        return results.findByResume_ResumeIdOrderByAnalyzedAtDescResultIdDesc(resumeId)
                .stream().map(Mapper::toAnalysis).toList();
    }

    @Transactional(readOnly = true)
    public List<AnalysisResponse> recent(int limit) {
        int size = Math.min(Math.max(limit, 1), 50);
        return results.findAllByOrderByAnalyzedAtDescResultIdDesc(PageRequest.of(0, size))
                .stream().map(Mapper::toAnalysis).toList();
    }
}
