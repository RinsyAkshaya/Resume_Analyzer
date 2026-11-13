package com.resumeanalyser.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "job_roles")
public class JobRole {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "job_id")
    private Long jobId;

    @Column(name = "role_name", nullable = false, unique = true, length = 100)
    private String roleName;

    /** Comma separated required skills, e.g. "Java, Spring Boot, SQL, Hibernate, REST API, OOP". */
    @Column(name = "required_skills", nullable = false, length = 600)
    private String requiredSkills;

    public JobRole() { }

    public JobRole(String roleName, String requiredSkills) {
        this.roleName = roleName;
        this.requiredSkills = requiredSkills;
    }

    public Long getJobId() { return jobId; }
    public void setJobId(Long jobId) { this.jobId = jobId; }
    public String getRoleName() { return roleName; }
    public void setRoleName(String roleName) { this.roleName = roleName; }
    public String getRequiredSkills() { return requiredSkills; }
    public void setRequiredSkills(String requiredSkills) { this.requiredSkills = requiredSkills; }
}
