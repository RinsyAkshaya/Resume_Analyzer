package com.resumeanalyser.service;

import com.resumeanalyser.dto.StatsResponse;
import com.resumeanalyser.repository.AnalysisResultRepository;
import com.resumeanalyser.repository.ResumeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StatsService {

    private final ResumeRepository resumes;
    private final AnalysisResultRepository results;

    public StatsService(ResumeRepository resumes, AnalysisResultRepository results) {
        this.resumes = resumes;
        this.results = results;
    }

    @Transactional(readOnly = true)
    public StatsResponse stats() {
        Double avg = results.averageMatchScore();
        double rounded = avg == null ? 0.0 : Math.round(avg * 10.0) / 10.0;
        return new StatsResponse(resumes.count(), results.countAnalysedResumes(), rounded,
                results.countShortlistedCandidates());
    }
}
