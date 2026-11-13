package com.resumeanalyser.service;

import com.resumeanalyser.dto.JobRoleResponse;
import com.resumeanalyser.repository.JobRoleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class JobService {

    private final JobRoleRepository jobs;

    public JobService(JobRoleRepository jobs) {
        this.jobs = jobs;
    }

    @Transactional(readOnly = true)
    public List<JobRoleResponse> list() {
        return jobs.findAllByOrderByJobIdAsc().stream().map(Mapper::toJob).toList();
    }
}
