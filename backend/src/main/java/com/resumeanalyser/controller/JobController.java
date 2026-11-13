package com.resumeanalyser.controller;

import com.resumeanalyser.dto.JobRoleResponse;
import com.resumeanalyser.service.JobService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/jobs")
public class JobController {

    private final JobService jobs;

    public JobController(JobService jobs) {
        this.jobs = jobs;
    }

    @GetMapping
    public List<JobRoleResponse> list() {
        return jobs.list();
    }
}
