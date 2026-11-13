package com.resumeanalyser.repository;

import com.resumeanalyser.entity.JobRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface JobRoleRepository extends JpaRepository<JobRole, Long> {
    Optional<JobRole> findByRoleName(String roleName);
    List<JobRole> findAllByOrderByJobIdAsc();
}
