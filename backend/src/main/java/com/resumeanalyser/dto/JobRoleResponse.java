package com.resumeanalyser.dto;

import java.util.List;

public record JobRoleResponse(Long jobId, String roleName, List<String> requiredSkills) { }
