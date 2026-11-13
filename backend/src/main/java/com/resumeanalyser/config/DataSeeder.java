package com.resumeanalyser.config;

import com.resumeanalyser.entity.JobRole;
import com.resumeanalyser.entity.User;
import com.resumeanalyser.repository.JobRoleRepository;
import com.resumeanalyser.repository.ResumeRepository;
import com.resumeanalyser.repository.UserRepository;
import com.resumeanalyser.service.AnalysisService;
import com.resumeanalyser.service.AuthService;
import com.resumeanalyser.service.ResumeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Seeds (idempotently) the six job roles, a demo user and the five sample candidates (A-E) from the PPT.
 * The sample resumes are ordinary text resumes: they go through the same parser and the same
 * matching code as any uploaded file, so their scores are calculated, not hard-coded.
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    public static final String DEMO_EMAIL = "demo@resumeanalyser.com";
    public static final String DEMO_PASSWORD = "Demo@1234";

    private final JobRoleRepository jobs;
    private final UserRepository users;
    private final ResumeRepository resumes;
    private final AuthService auth;
    private final ResumeService resumeService;
    private final AnalysisService analysis;

    public DataSeeder(JobRoleRepository jobs, UserRepository users, ResumeRepository resumes, AuthService auth,
                      ResumeService resumeService, AnalysisService analysis) {
        this.jobs = jobs;
        this.users = users;
        this.resumes = resumes;
        this.auth = auth;
        this.resumeService = resumeService;
        this.analysis = analysis;
    }

    private record Sample(String name, String skills, int years, String education) { }

    @Override
    public void run(String... args) {
        seedJobs();
        User demo = users.findByEmailIgnoreCase(DEMO_EMAIL)
                .orElseGet(() -> auth.createUser("Demo HR Manager", DEMO_EMAIL, DEMO_PASSWORD));
        seedSamples(demo);
    }

    private void seedJobs() {
        List<JobRole> roles = List.of(
                new JobRole("Java Developer", "Java, Spring Boot, SQL, Hibernate, REST API, OOP"),
                new JobRole("Python Developer", "Python, Django, Flask, SQL, REST API, Git"),
                new JobRole("Data Analyst", "Python, SQL, Excel, Power BI, Pandas, Statistics"),
                new JobRole("Web Developer", "HTML, CSS, JavaScript, SQL, REST API"),
                new JobRole("Software Engineer", "Java, Data Structures, Algorithms, OOP, SQL, Git"),
                new JobRole("Full Stack Developer", "HTML, CSS, JavaScript, Java, Spring Boot, SQL, REST API"));
        for (JobRole role : roles) {
            if (jobs.findByRoleName(role.getRoleName()).isEmpty()) jobs.save(role);
        }
    }

    private void seedSamples(User owner) {
        List<Sample> samples = List.of(
                new Sample("Candidate A", "Java, Spring Boot, SQL", 2, "B.Tech CSE"),
                new Sample("Candidate B", "Python, ML, Pandas", 1, "B.Tech IT"),
                new Sample("Candidate C", "HTML, CSS, JavaScript", 3, "B.Sc CS"),
                new Sample("Candidate D", "Java, Hibernate, MySQL", 4, "M.Tech CSE"),
                new Sample("Candidate E", "C++, DSA, Git", 0, "B.Tech CSE"));

        for (int i = 0; i < samples.size(); i++) {
            Sample s = samples.get(i);
            String fileName = "sample-" + s.name().toLowerCase().replace(' ', '-') + ".txt";
            if (resumes.existsByUploadedFileName(fileName)) continue;

            String text = s.name() + "\n"
                    + "Email: " + s.name().toLowerCase().replace(' ', '.') + "@example.com\n"
                    + "Phone: +91 98765432" + (10 + i) + "\n\n"
                    + "Skills\n" + s.skills() + "\n\n"
                    + "Experience\n" + s.years() + " years of experience\n\n"
                    + "Education\n" + s.education() + "\n";

            var resume = resumeService.createFromText(fileName, text, owner);
            for (JobRole role : jobs.findAllByOrderByJobIdAsc()) {
                analysis.analyse(resume.getResumeId(), role.getJobId());
            }
            log.info("Seeded sample resume {}", s.name());
        }
    }
}
