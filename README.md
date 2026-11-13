# Resume Analyser (Java Swing + Spring Boot + MySQL)

```
Java Swing Frontend  ->  Spring Boot Controller  ->  Service Layer  ->  Spring Data JPA Repository  ->  MySQL
        (java.net.http.HttpClient, REST/JSON)
```

## Project layout
```
resume-analyser/
├── pom.xml                      aggregator (open this in IntelliJ IDEA)
├── database/schema.sql          optional manual DDL
├── backend/                     Spring Boot 3.3 / Java 21 / Maven
│   └── src/main/java/com/resumeanalyser/
│       ├── controller/  AuthController, ResumeController, AnalysisController, JobController, RankingController
│       ├── service/     AuthService, ResumeService, ResumeParserService (PDFBox + POI), SkillCatalog,
│       │                MatchCalculator, AnalysisService, RankingService, JobService, StatsService
│       ├── repository/  User / Resume / JobRole / AnalysisResult repositories (Spring Data JPA)
│       ├── entity/      User, Resume, JobRole, AnalysisResult
│       ├── dto/  exception/  config/ (BCrypt bean, DataSeeder)
└── frontend/                    Java Swing client (Jackson only for JSON)
    └── src/main/java/com/resumeanalyser/
        ├── api/   ApiClient (HttpClient), ApiException
        └── ui/    Login, Register, Dashboard, Upload, JobRole, Analysis, Ranking screens
```

## 1. Prerequisites
JDK 21, Maven 3.9+ (IntelliJ bundles Maven), MySQL 8.x running on localhost:3306.

## 2. Database
The backend creates the database and tables automatically (`createDatabaseIfNotExist=true`, `ddl-auto=update`).
Just make sure MySQL is running and set your credentials (defaults: user `root`, password `root`):

* edit `backend/src/main/resources/application.properties`, **or**
* set environment variables `DB_USERNAME`, `DB_PASSWORD` (and optionally `DB_URL`).

Optional manual setup: `mysql -u root -p < database/schema.sql`.

## 3. Run the backend
IntelliJ: open the root `pom.xml` -> run `ResumeAnalyserApplication`.
Terminal: `cd backend && mvn spring-boot:run`  (listens on http://localhost:8080)

On first start it seeds the 6 job roles, the demo user and the 5 PPT sample candidates (A-E),
each analysed against every role with the real matching code.

## 4. Run the frontend
IntelliJ: run `com.resumeanalyser.ResumeAnalyserApp` (module `frontend`).
Terminal: `cd frontend && mvn compile exec:java`
Runnable jar: `cd frontend && mvn package && java -jar target/resume-analyser-frontend-1.0.0-all.jar`
Other backend URL: `-Dapi.url=http://host:8080` or env `API_URL`.

## 5. Demo login
`demo@resumeanalyser.com` / `Demo@1234` (or register your own account).

## 6. How scoring works
* Text is extracted from PDF (PDFBox), DOCX (Apache POI) or TXT; name, email, phone, education, experience
  and skills are extracted with patterns + a skill dictionary (`SkillCatalog`).
* `Match Score = matched required skills / total required skills x 100` (rounded to 1 decimal).
* Shortlisted when score >= `analysis.shortlist-threshold` (default 50).
* MySQL/PostgreSQL/Oracle/SQLite/SQL Server on a resume also count as the **SQL** skill; GitHub counts as **Git**.
* Re-analysing the same resume for the same role updates the stored result.
* The Ranking screen has a "PPT Illustrative Sample" tab with the 91/82/56/48/35 % figures, clearly labelled as
  illustrative. The "Live Ranking" tab is calculated from the database.

## 7. REST API
| Method | Path |
|---|---|
| POST | `/api/auth/register` `{fullName,email,password,confirmPassword}` |
| POST | `/api/auth/login` `{email,password}` |
| POST | `/api/resumes/upload` multipart `file`, optional `userId` |
| POST | `/api/analyse/{resumeId}?jobId=` |
| GET  | `/api/resumes` |
| GET  | `/api/results/{resumeId}` ; `/api/results/recent?limit=` |
| GET  | `/api/jobs` |
| GET  | `/api/ranking/{jobId}` |
| GET  | `/api/stats` |
