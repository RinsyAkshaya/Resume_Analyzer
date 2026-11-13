package com.resumeanalyser.dto;

import java.util.List;

public record RankingEntry(
        int rank,
        Long resumeId,
        String candidateName,
        String email,
        Double matchScore,
        List<String> matchedSkills,
        List<String> missingSkills,
        Double experienceYears,
        String education,
        Boolean shortlisted,
        String status) { }
