package com.resumeanalyser.repository;

import com.resumeanalyser.entity.AnalysisResult;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface AnalysisResultRepository extends JpaRepository<AnalysisResult, Long> {

    Optional<AnalysisResult> findByResume_ResumeIdAndJobRole_JobId(Long resumeId, Long jobId);

    List<AnalysisResult> findByResume_ResumeIdOrderByAnalyzedAtDescResultIdDesc(Long resumeId);

    List<AnalysisResult> findByJobRole_JobId(Long jobId);

    List<AnalysisResult> findAllByOrderByAnalyzedAtDescResultIdDesc(Pageable pageable);

    @Query("select count(distinct a.resume.resumeId) from AnalysisResult a")
    long countAnalysedResumes();

    @Query("select avg(a.matchScore) from AnalysisResult a")
    Double averageMatchScore();

    @Query("select count(distinct a.resume.resumeId) from AnalysisResult a where a.shortlisted = true")
    long countShortlistedCandidates();
}
