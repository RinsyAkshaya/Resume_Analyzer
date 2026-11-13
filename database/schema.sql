-- Optional: the backend creates all of this automatically (spring.jpa.hibernate.ddl-auto=update).
-- Run this file only if you prefer to create the schema by hand.
CREATE DATABASE IF NOT EXISTS resume_analyser CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE resume_analyser;

CREATE TABLE IF NOT EXISTS users (
    user_id     BIGINT AUTO_INCREMENT PRIMARY KEY,
    full_name   VARCHAR(120) NOT NULL,
    email       VARCHAR(190) NOT NULL UNIQUE,
    password    VARCHAR(100) NOT NULL,            -- BCrypt hash
    created_at  DATETIME(6)  NOT NULL
);

CREATE TABLE IF NOT EXISTS resumes (
    resume_id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    candidate_name     VARCHAR(150) NOT NULL,
    email              VARCHAR(190),
    phone              VARCHAR(40),               -- extra column: extracted phone number
    skills             TEXT,
    experience_years   DOUBLE NOT NULL DEFAULT 0,
    education          VARCHAR(500),
    uploaded_file_name VARCHAR(255) NOT NULL,
    uploaded_at        DATETIME(6)  NOT NULL,
    user_id            BIGINT,
    CONSTRAINT fk_resume_user FOREIGN KEY (user_id) REFERENCES users (user_id)
);

CREATE TABLE IF NOT EXISTS job_roles (
    job_id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    role_name       VARCHAR(100) NOT NULL UNIQUE,
    required_skills VARCHAR(600) NOT NULL
);

CREATE TABLE IF NOT EXISTS analysis_results (
    result_id      BIGINT AUTO_INCREMENT PRIMARY KEY,
    resume_id      BIGINT NOT NULL,
    job_id         BIGINT NOT NULL,
    match_score    DOUBLE NOT NULL,
    matched_skills TEXT,
    missing_skills TEXT,
    shortlisted    BIT NOT NULL,
    analyzed_at    DATETIME(6) NOT NULL,
    CONSTRAINT uk_resume_job UNIQUE (resume_id, job_id),
    CONSTRAINT fk_result_resume FOREIGN KEY (resume_id) REFERENCES resumes (resume_id),
    CONSTRAINT fk_result_job    FOREIGN KEY (job_id)    REFERENCES job_roles (job_id)
);
-- Job roles, the demo user (demo@resumeanalyser.com / Demo@1234) and the five PPT sample candidates
-- are inserted by the backend on first start (DataSeeder), because the demo password must be BCrypt-hashed.
