package com.resumeanalyser.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public record ResumeResponse(
        Long resumeId,
        String candidateName,
        String email,
        String phone,
        List<String> skills,
        Map<String, List<String>> technicalSkills,
        Double experienceYears,
        String education,
        String uploadedFileName,
        LocalDateTime uploadedAt) { }
