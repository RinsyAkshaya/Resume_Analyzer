package com.resumeanalyser.service;

import com.resumeanalyser.dto.ResumeResponse;
import com.resumeanalyser.entity.Resume;
import com.resumeanalyser.entity.User;
import com.resumeanalyser.exception.ApiException;
import com.resumeanalyser.repository.ResumeRepository;
import com.resumeanalyser.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Set;

@Service
public class ResumeService {

    private static final Set<String> ALLOWED = Set.of("pdf", "docx", "txt");

    private final ResumeRepository resumes;
    private final UserRepository users;
    private final ResumeParserService parser;

    public ResumeService(ResumeRepository resumes, UserRepository users, ResumeParserService parser) {
        this.resumes = resumes;
        this.users = users;
        this.parser = parser;
    }

    @Transactional
    public ResumeResponse upload(MultipartFile file, Long userId) {
        if (file == null || file.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Please choose a resume file to upload.");
        }
        String name = file.getOriginalFilename() == null ? "resume.txt" : file.getOriginalFilename();
        if (!ALLOWED.contains(ResumeParserService.extension(name))) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Unsupported file type. Please upload a PDF, DOCX or TXT file.");
        }
        User owner = null;
        if (userId != null) {
            owner = users.findById(userId).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found."));
        }
        byte[] bytes;
        try {
            bytes = file.getBytes();
        } catch (IOException e) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Could not read the uploaded file.");
        }
        String text = parser.extractText(name, bytes);
        return Mapper.toResume(save(parser.parseText(name, text), name, owner));
    }

    /** Used by the demo data seeder (plain text resumes). */
    @Transactional
    public Resume createFromText(String fileName, String text, User owner) {
        return save(parser.parseText(fileName, text), fileName, owner);
    }

    private Resume save(ResumeParserService.ParsedResume p, String fileName, User owner) {
        Resume r = new Resume();
        r.setCandidateName(trim(p.candidateName(), 150));
        r.setEmail(trim(p.email(), 190));
        r.setPhone(trim(p.phone(), 40));
        r.setSkills(CsvUtil.join(p.skills()));
        r.setExperienceYears(p.experienceYears());
        r.setEducation(trim(p.education(), 500));
        r.setUploadedFileName(trim(fileName, 255));
        r.setUser(owner);
        return resumes.save(r);
    }

    @Transactional(readOnly = true)
    public List<ResumeResponse> list() {
        return resumes.findAllByOrderByUploadedAtDescResumeIdDesc().stream().map(Mapper::toResume).toList();
    }

    private static String trim(String s, int max) {
        if (s == null) return null;
        return s.length() <= max ? s : s.substring(0, max);
    }
}
