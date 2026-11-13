package com.resumeanalyser.dto;

import java.time.LocalDateTime;
import java.util.List;

public record AnalysisResponse(
        Long resultId,
        Long resumeId,
        String candidateName,
        String email,
        Long jobId,
        String roleName,
        List<String> requiredSkills,
        Double matchScore,
        List<String> matchedSkills,
        List<String> missingSkills,
        String education,
        Double experienceYears,
        Boolean shortlisted,
        String status,
        LocalDateTime analyzedAt) { }
