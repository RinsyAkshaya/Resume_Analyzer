package com.resumeanalyser.service;

import com.resumeanalyser.dto.AnalysisResponse;
import com.resumeanalyser.dto.JobRoleResponse;
import com.resumeanalyser.dto.ResumeResponse;
import com.resumeanalyser.entity.AnalysisResult;
import com.resumeanalyser.entity.JobRole;
import com.resumeanalyser.entity.Resume;

import java.util.List;

/** Entity -> DTO conversion. Call inside a transaction (lazy relations). */
public final class Mapper {

    private Mapper() { }

    public static ResumeResponse toResume(Resume r) {
        List<String> skills = CsvUtil.split(r.getSkills());
        return new ResumeResponse(r.getResumeId(), r.getCandidateName(), r.getEmail(), r.getPhone(), skills,
                SkillCatalog.technicalByCategory(skills), r.getExperienceYears(), r.getEducation(),
                r.getUploadedFileName(), r.getUploadedAt());
    }

    public static JobRoleResponse toJob(JobRole j) {
        return new JobRoleResponse(j.getJobId(), j.getRoleName(), CsvUtil.split(j.getRequiredSkills()));
    }

    public static String status(boolean shortlisted) {
        return shortlisted ? "Shortlisted" : "Not Shortlisted";
    }

    public static AnalysisResponse toAnalysis(AnalysisResult a) {
        Resume r = a.getResume();
        JobRole j = a.getJobRole();
        return new AnalysisResponse(a.getResultId(), r.getResumeId(), r.getCandidateName(), r.getEmail(),
                j.getJobId(), j.getRoleName(), CsvUtil.split(j.getRequiredSkills()), a.getMatchScore(),
                CsvUtil.split(a.getMatchedSkills()), CsvUtil.split(a.getMissingSkills()), r.getEducation(),
                r.getExperienceYears(), a.getShortlisted(), status(a.getShortlisted()), a.getAnalyzedAt());
    }
}
