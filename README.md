# SkillGap AI

Upload a resume (PDF or pasted text) and paste a job description; get back a
report showing which required skills are **matched**, which are **missing**
(ranked by how easy each is to close), and which skills you have but didn't
clearly state on your resume (**underemphasized**). Every result comes with
a plain-language reason, never a bare verdict.

## Problem it solves

Students apply to roles without knowing how well their resume actually
matches a specific job description. Generic online "resume checkers" give
vague, generic advice instead of a structured, explainable comparison
against a real, specific JD.

## Why it's built this way

Every technology here is deliberately Java. The skill-matching logic is
transparent, rule-based Java code — exact match, then known synonyms, then
fuzzy/typo tolerance — not a black-box AI model. No external AI API, no
Python, no ML model. That makes the whole thing fully explainable end to
end: every match or gap in a report traces back to a specific, readable rule
you can point to and defend.

## Tech stack

| Layer | Technology |
|---|---|
| Frontend | React, Tailwind CSS |
| Backend | Java 21, Spring Boot, Spring Security (JWT), Spring Data JPA |
| Database | MySQL |
| Resume parsing | Apache PDFBox |
| Matching logic | Plain Java + hand-written Jaro-Winkler string similarity |
| PDF export | Apache PDFBox (server-side report rendering) |
| Build tools | Maven (via Maven Wrapper — no system Maven needed), npm, Git |

## Architecture

```text
User (Browser)
      |
      v
React Frontend
   Auth  |  Upload & Compare  |  Processing  |  Gap Report  |  History
      |
      |  REST API (JSON over HTTPS), JWT in the Authorization header
      v
Spring Security — JwtAuthenticationFilter
      |  rejects anything without a valid token, except /api/auth/**
      v
Controller Layer
   AuthController | MatchController | ResumeController
   ReportController | SkillsController
      |
      v
Service Layer
   AuthService            - signup/login, BCrypt password hashing, JWT issuing
   ResumeParsingService   - Apache PDFBox: resume PDF -> plain text
   SkillMatchingService   - taxonomy + string similarity (exact/synonym/fuzzy)
   ReportService          - builds the ranked gap report, persists it
   GapReportPdfService    - Apache PDFBox: report -> downloadable PDF
      |
      v
Repository Layer (Spring Data JPA)
      |
      v
MySQL Database (skillgap_ai)
   users | resumes | gap_reports | skills_taxonomy
```

## Features

- **Real accounts** — signup/login with BCrypt-hashed passwords and JWT
  auth; every report and history entry is scoped to your own account.
- **Resume input, two ways** — upload a PDF (parsed server-side with
  PDFBox) with a manual-paste fallback if extraction fails or the file has
  no text layer.
- **Skill checklist** built from a taxonomy of common skills; tick what a
  JD requires and mark each as core or nice-to-have.
- **Rule-based matching, not a black box** — exact match, then known
  synonyms (e.g. "JS" for JavaScript), then fuzzy/typo tolerance, in that
  order. Every result states *why*.
- **Gap Report** — overall match percentage, matched skills, missing
  skills ranked easiest-to-close-first, and underemphasized skills (a core
  requirement mentioned only once).
- **History** — every past report, most recent first, reopenable in full.
- **PDF export** — download any report as a clean, paginated PDF,
  generated server-side.
- **Accessible** — WCAG AA contrast, full keyboard navigation, labeled
  form fields, and a designed empty/error state everywhere something could
  go wrong (network down, invalid file, empty results).

## Setup

### Prerequisites

- **Java 21+** (JDK)
- **Node.js 20.19+ or 22.12+** (required by Vite 8) and npm
- **MySQL 8.x**, running locally
- Git

### 1. Clone the repo

```bash
git clone <this-repo-url>
cd skillgap-ai
```

### 2. Create the database

Hibernate creates and updates all four tables automatically on first run
(`spring.jpa.hibernate.ddl-auto=update`) — you only need to create the empty
database itself:

```sql
CREATE DATABASE skillgap_ai;
```

### 3. Configure the backend

Copy the committed template and fill in your own values — this file is
gitignored, so your credentials and secret never get committed:

```bash
cd backend
cp src/main/resources/application-local.properties.example src/main/resources/application-local.properties
```

Edit `application-local.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/skillgap_ai
spring.datasource.username=root
spring.datasource.password=your-mysql-password-here

# Generate your own with: openssl rand -base64 32
jwt.secret=replace-with-your-own-generated-secret
```

### 4. Run the backend

No system-wide Maven needed — the project uses the Maven Wrapper, which
downloads its own pinned Maven version on first run:

```bash
./mvnw spring-boot:run      # macOS/Linux
mvnw.cmd spring-boot:run    # Windows
```

The backend starts on **http://localhost:8080**. On first startup, watch
the log for `HikariPool-1 - Start completed` (MySQL connected) and
`Tomcat started on port 8080`.

### 5. Run the frontend

In a second terminal:

```bash
cd frontend
npm install
npm run dev
```

The frontend starts on **http://localhost:5173** and proxies `/api/*`
requests to the backend at `:8080` — no CORS configuration needed for
local development.

### 6. Use it

Open **http://localhost:5173**, sign up for an account, and go.

## Verifying the build

```bash
# Backend compiles
cd backend && ./mvnw compile          # or mvnw.cmd compile

# Frontend builds for production
cd frontend && npm run build

# Phase 1's original plain-Java matching demo (no Spring/DB required)
cd backend && ./mvnw exec:java        # or mvnw.cmd exec:java
```

## Project layout

```text
skillgap-ai/
├── backend/
│   └── src/main/java/com/skillgapai/
│       ├── model/        - plain data classes (Skill, MatchResult, ...)
│       ├── taxonomy/     - the skill taxonomy (canonical names + synonyms + difficulty)
│       ├── matching/     - SkillMatchingService + hand-written string similarity
│       ├── parsing/      - PDFBox resume text extraction
│       ├── export/       - PDFBox report -> PDF generation
│       ├── security/     - JWT issuing/verification, the auth filter
│       ├── config/       - Spring Security config, taxonomy bean
│       ├── entity/       - JPA entities (User, Resume, GapReport, ...)
│       ├── repository/   - Spring Data JPA repositories
│       ├── service/      - AuthService, ReportService
│       ├── dto/          - request/response shapes
│       └── web/          - REST controllers
└── frontend/
    └── src/
        ├── components/   - one component per screen/section
        ├── api.js        - all backend calls, in one place
        └── App.jsx        - top-level auth gate + screen routing
```

## Demo

See [`DEMO_SCRIPT.md`](DEMO_SCRIPT.md) for a rehearsed, 3–5 minute walkthrough
suitable for a viva or interview.
