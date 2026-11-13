package com.resumeanalyser.service;

import com.resumeanalyser.dto.RankingEntry;
import com.resumeanalyser.entity.AnalysisResult;
import com.resumeanalyser.exception.ApiException;
import com.resumeanalyser.repository.AnalysisResultRepository;
import com.resumeanalyser.repository.JobRoleRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class RankingService {

    private final AnalysisResultRepository results;
    private final JobRoleRepository jobs;

    public RankingService(AnalysisResultRepository results, JobRoleRepository jobs) {
        this.results = results;
        this.jobs = jobs;
    }

    /** Candidates analysed for the role, highest match score first (ties: more experience first, then name). */
    @Transactional(readOnly = true)
    public List<RankingEntry> ranking(Long jobId) {
        if (!jobs.existsById(jobId)) throw new ApiException(HttpStatus.NOT_FOUND, "Job role not found.");
        List<AnalysisResult> list = new ArrayList<>(results.findByJobRole_JobId(jobId));
        list.sort(Comparator
                .comparing(AnalysisResult::getMatchScore, Comparator.reverseOrder())
                .thenComparing(a -> a.getResume().getExperienceYears(), Comparator.reverseOrder())
                .thenComparing(a -> a.getResume().getCandidateName(), String.CASE_INSENSITIVE_ORDER));

        List<RankingEntry> out = new ArrayList<>();
        int rank = 1;
        for (AnalysisResult a : list) {
            var r = a.getResume();
            out.add(new RankingEntry(rank++, r.getResumeId(), r.getCandidateName(), r.getEmail(), a.getMatchScore(),
                    CsvUtil.split(a.getMatchedSkills()), CsvUtil.split(a.getMissingSkills()), r.getExperienceYears(),
                    r.getEducation(), a.getShortlisted(), Mapper.status(a.getShortlisted())));
        }
        return out;
    }
}
