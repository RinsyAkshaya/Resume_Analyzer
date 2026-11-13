package com.resumeanalyser.controller;

import com.resumeanalyser.dto.ResumeResponse;
import com.resumeanalyser.service.ResumeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/resumes")
public class ResumeController {

    private final ResumeService resumes;

    public ResumeController(ResumeService resumes) {
        this.resumes = resumes;
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ResumeResponse> upload(@RequestParam("file") MultipartFile file,
                                                 @RequestParam(value = "userId", required = false) Long userId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(resumes.upload(file, userId));
    }

    @GetMapping
    public List<ResumeResponse> list() {
        return resumes.list();
    }
}
