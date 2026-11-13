package com.resumeanalyser.dto;

public record StatsResponse(long totalResumes, long resumesAnalysed, double averageMatchScore, long shortlistedCandidates) { }
