# SkillGap AI — Project Memory

This file is read automatically by Claude Code at the start of every session
in this folder. It tells Claude how to work on this project so you don't
have to re-explain the rules every time.

## Who's building this

Gunjan Senapati, B.Tech CSE (2023–2027), KIST Bhubaneswar. Comfortable with
Java fundamentals, OOP, basic Spring Boot, HTML/CSS/JS/React, and MySQL —
but still developing these skills, not an expert. Treat me as a
beginner-to-developing programmer who wants to understand and defend this
project in a college viva and a job interview, not just have it built for me.

## Working agreement — read this before writing any code

- Build in the phases listed below, ONE AT A TIME. Never generate multiple
  phases at once, even if I say "just finish it" — confirm with me first.
- After each phase, actually run the relevant build/test command and show
  me the real output. Do not assume something works — prove it.
- Explain the reasoning behind each significant decision before moving to
  the next phase.
- Prefer the simple approach over the clever one. If a cleverer alternative
  exists, mention it briefly rather than using it by default.
- Do not introduce tools/frameworks outside the tech stack below without
  asking first and explaining why.
- When my code fails, don't immediately rewrite everything: identify the
  error, explain what it means, find the root cause, apply the smallest
  fix, explain why it works, then re-test.
- Preserve existing working code/structure unless a change is genuinely
  necessary — explain before doing a major redesign.

## Project overview

**One-line idea:** Upload a resume (PDF) and paste a job description; get
back a report showing which required skills are matched, which are missing
(ranked by how easy each is to close), and which skills the user has but
didn't clearly state on the resume.

**Problem it solves:** Students apply to roles without knowing how well
their resume actually matches a specific JD. Generic online "resume
checkers" give vague advice instead of a structured comparison against a
real, specific JD.

**Why it's built this way:** Every technology is deliberately Java, and the
skill-matching logic is transparent, rule-based Java code — not a black-box
AI model. A rule-based system is fully explainable in a viva, and a
fresher's resume value here comes from demonstrable Spring Boot/JPA/MySQL
proficiency.

## Tech stack (non-negotiable — ask before adding anything else)

| Layer | Technology |
|---|---|
| Frontend | React, Tailwind CSS, Recharts |
| Backend | Java, Spring Boot, Spring Security (JWT), Spring Data JPA |
| Database | MySQL |
| Resume parsing | Apache PDFBox |
| Matching logic | Plain Java + string similarity (Levenshtein / Jaro-Winkler) |
| Build tools | Maven (backend), npm (frontend), Git/GitHub |

No Python. No external AI API. No embeddings/ML model for the MVP.

## System architecture

```text
User (Browser)
      |
      v
React Frontend (Upload / Processing / Report / History / Auth)
      |  REST API (JSON over HTTPS)
      v
Spring Boot Controller Layer
      |
      v
Service Layer
   +-- ResumeParsingService   (Apache PDFBox)
   +-- SkillMatchingService   (taxonomy + string similarity)
   +-- ReportService          (builds the ranked gap report)
      |
      v
Repository Layer (Spring Data JPA)
      |
      v
MySQL Database
```

## Database schema

```text
User (user_id, name, email, password_hash)
Resume (resume_id, user_id, extracted_text, uploaded_at)
SkillsTaxonomy (skill_id, canonical_name, synonyms)
GapReport (report_id, resume_id, jd_text, matched_skills_json, missing_skills_json,
           underemphasized_skills_json, match_percentage, created_at)
```

## UI/UX principles for every screen

- Clarity over decoration — every screen answers "what do I do here" in 2 seconds.
- Visible reasoning — never show a match result without showing *why*.
- Calm confidence — muted, professional palette, not a gradient-heavy "AI startup" look.
- Responsive feedback — every action gets a visible state change.
- Accessibility is non-negotiable: WCAG AA contrast, full keyboard nav, alt text, labeled fields.

Screens: Landing, Upload & Compare, Processing, Gap Report (the hero screen),
History, Auth.

## Build order (work through ONE phase at a time)

1. Plain Java matching logic (no Spring, no DB) — **DONE, see status below**
2. MySQL schema + Spring Boot skeleton — **DONE, see status below**
3. Wire matching logic into `POST /api/match`, tested via curl — no persistence yet — **NEXT**
4. Persistence: save each match as a GapReport row, add `GET /api/reports/{id}`
5. Resume PDF parsing (PDFBox) with manual-paste fallback
6. React screens, one at a time: 6a Upload & Compare, 6b Processing, 6c Gap Report, 6d History, 6e Auth
7. Apply the visual design system consistently — no functional changes
8. Accessibility pass + empty/error states
9. PDF export of the report
10. README + demo script, tag `v1.0`

Commit after each phase with a message describing what that phase delivered.

## Current status (read this first in every session)

**Phase 1 is complete.** Plain Java skill-matching logic lives in
`backend/src/main/java/com/skillgapai/`:

- `model/` — Skill, RequiredSkill, MatchResult, MatchType, GapAnalysisResult, Difficulty
- `taxonomy/SkillsTaxonomy.java` — canonical skill names + synonyms + a difficulty rating
- `matching/SkillMatchingService.java` — the core matching algorithm (exact → synonym → fuzzy, in that order)
- `matching/StringSimilarity.java` — a hand-written Jaro-Winkler similarity function
- `Phase1Demo.java` — a runnable demo with a hardcoded sample resume + JD

It currently builds with plain `javac`/`java` (no Maven needed yet — zero
external dependencies). It was built and tested in a cloud sandbox that
could not reach Maven Central, so Maven itself was never actually run there
— **on this machine, with normal internet access, switch Phase 2 onward to
real Maven + Spring Boot as originally planned.**

Two real bugs were caught by running the demo and reading the actual
output, then fixed:

- "React" was fuzzy-matched to the unrelated word "recent" (86% similar).
  Fixed by raising the fuzzy-match threshold from 0.85 to 0.92.
- "Spring Security" was fuzzy-matched to "Spring Boot" (a different, real
  skill). Fixed by rejecting any fuzzy candidate that is itself a known
  name of a different skill in the taxonomy.

**Phase 2 is complete.** The backend is now a real Spring Boot app (Maven
Wrapper, no system-wide Maven needed — see below), connected to a local
MySQL database called `skillgap_ai`:

- `pom.xml` now has a `spring-boot-starter-parent` and pulls in
  `spring-boot-starter-web`, `spring-boot-starter-data-jpa`, and
  `mysql-connector-j`. Phase 1's plain-Java classes and its
  `commons-text` dependency are untouched.
- `SkillGapAiApplication.java` — the `@SpringBootApplication` entry point.
  Run it with `./mvnw spring-boot:run` (`mvnw.cmd` on Windows). Phase 1's
  `Phase1Demo` still works unchanged via `mvnw exec:java`.
- `entity/` — JPA `@Entity` classes for the four schema tables: `User`,
  `Resume`, `GapReport`, and `SkillTaxonomyEntry`. That last one is
  deliberately not named `SkillsTaxonomy` — that name is already taken by
  the plain-Java class in `taxonomy/`, and a later phase will likely need
  both imported in the same file.
- `repository/` — one empty `JpaRepository<Entity, Long>` interface per
  entity (`UserRepository`, `ResumeRepository`, `GapReportRepository`,
  `SkillTaxonomyEntryRepository`). No custom queries yet - Phase 3/4 will
  add methods as real endpoints need them.
- `spring.jpa.hibernate.ddl-auto=update` — Hibernate creates/updates
  tables from the `@Entity` classes; there's no separate SQL schema file
  to keep in sync by hand.
- MySQL credentials live in `application-local.properties`, which is
  gitignored (see `.gitignore`). `application-local.properties.example`
  is the committed template showing what keys are needed - copy it and
  fill in your own password to run this locally.
- Maven itself is NOT installed system-wide on this machine. The project
  uses the Maven Wrapper instead (`mvnw` / `mvnw.cmd` +
  `.mvn/wrapper/`), which downloads its own pinned Maven version on
  first run. Always invoke Maven as `./mvnw ...` (or `mvnw.cmd ...` on
  plain Windows cmd/PowerShell), never bare `mvn`.

One real bug was caught by running the app and reading the actual schema
(not just trusting the entity code): Hibernate's default MySQL mapping
for a `@Lob` `String` field is `TINYTEXT` (max 255 bytes) unless told
otherwise. That's nowhere near big enough for a resume's extracted text,
a pasted JD, or the JSON skill lists. Fixed by adding
`columnDefinition = "LONGTEXT"` (or `TEXT` for the shorter `synonyms`
column) to every `@Lob` field, then dropping and letting Hibernate
recreate the four tables so the column types actually changed.

Verified: `./mvnw spring-boot:run` starts cleanly, connects to MySQL
(HikariPool + Tomcat both start with no errors), and
`SHOW TABLES` / `DESCRIBE` against `skillgap_ai` confirms all four
tables exist with the corrected column types.

**Next up: Phase 3** — wire the existing matching logic into a
`POST /api/match` endpoint, tested via curl. Still no persistence at
this stage (that's Phase 4) - the point of Phase 3 is proving the
Spring MVC layer can call `SkillMatchingService` end-to-end over HTTP.

## Definition of "outstanding" — check every screen against this

- Understandable with zero explanation within 5 seconds
- Every result shows *why*, not just *that*
- Visible feedback for every user action
- I can explain, in my own words, why it looks and works this way
