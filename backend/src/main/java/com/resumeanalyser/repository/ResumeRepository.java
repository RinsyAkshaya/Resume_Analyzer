package com.resumeanalyser.repository;

import com.resumeanalyser.entity.Resume;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ResumeRepository extends JpaRepository<Resume, Long> {
    List<Resume> findAllByOrderByUploadedAtDescResumeIdDesc();
    boolean existsByUploadedFileName(String uploadedFileName);
}
